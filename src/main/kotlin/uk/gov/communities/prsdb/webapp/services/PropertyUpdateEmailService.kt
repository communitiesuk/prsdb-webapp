package uk.gov.communities.prsdb.webapp.services

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordPropertyUpdateNotificationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordPropertyUpdateWithLettingAgentRemovedNotification
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LettingAgentPropertyUpdateNotificationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateWithLettingAgentRemovedConfirmation

@PrsdbWebService
class PropertyUpdateEmailService(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val userToLandlordService: UserToLandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailService: EmailNotificationService<PropertyUpdateConfirmation>,
    private val notificationEmailService: EmailNotificationService<JointLandlordPropertyUpdateNotificationEmail>,
    private val lettingAgentRemovedEmailService: EmailNotificationService<PropertyUpdateWithLettingAgentRemovedConfirmation>,
    private val jointLandlordLettingAgentRemovedEmailService: EmailNotificationService<
        JointLandlordPropertyUpdateWithLettingAgentRemovedNotification,
        >,
    private val lettingAgentUpdateEmailService: EmailNotificationService<LettingAgentPropertyUpdateNotificationEmail>,
    private val currentUserService: CurrentUserService,
) {
    fun sendUpdateEmails(
        propertyId: Long,
        updatedBullets: List<String>,
    ) {
        val actingLandlord = userToLandlordService.getCurrentLandlordForUserOrNull()
        if (actingLandlord == null) {
            if (propertyOwnershipService.getCurrentUserIsAuthorizedToEditRecord(propertyId)) {
                sendLettingAgentUpdateEmails(propertyId, updatedBullets)
                return
            }
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No acting landlord was found for the update to property ownership $propertyId",
            )
        }
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
        val registrationNumber =
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString()

        confirmationEmailService.sendEmail(
            currentUserService.getCurrentEmail(),
            PropertyUpdateConfirmation(
                singleLineAddress = propertyOwnership.address.singleLineAddress,
                registrationNumber = registrationNumber,
                updatedBullets = updatedBullets,
                dashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri(),
            ),
        )
        // TODO: PDJB-1274: Update emails to account for other users in the org

        val otherLandlords = propertyOwnership.landlords.filter { it.id != actingLandlord.id }
        if (otherLandlords.isNotEmpty()) {
            val propertyRecordUrl = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id).toString()
            otherLandlords.forEach { landlord ->
                notificationEmailService.sendEmailToLandlord(
                    landlord,
                    JointLandlordPropertyUpdateNotificationEmail(
                        recipientName = landlord.name,
                        propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                        updatedBullets = updatedBullets,
                        propertyRecordUrl = propertyRecordUrl,
                    ),
                )
            }
        }
    }

    private fun sendLettingAgentUpdateEmails(
        propertyId: Long,
        updatedBullets: List<String>,
    ) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
        val registrationNumber =
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString()
        val propertyRecordUrl = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id).toString()
        propertyOwnership.landlords.forEach { landlord ->
            lettingAgentUpdateEmailService.sendEmailToLandlord(
                landlord,
                LettingAgentPropertyUpdateNotificationEmail(
                    recipientName = landlord.name,
                    propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                    registrationNumber = registrationNumber,
                    updatedBullets = updatedBullets,
                    propertyRecordUrl = propertyRecordUrl,
                ),
            )
        }
    }

    fun sendLettingAgentUpdateEmailsIfLettingAgentUpdated(
        propertyId: Long,
        updatedBullets: List<String>,
    ) {
        val actingLandlord = userToLandlordService.getCurrentLandlordForUserOrNull()
        if (actingLandlord != null) {
            return
        }
        if (!propertyOwnershipService.getCurrentUserIsAuthorizedToEditRecord(propertyId)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No acting landlord was found for the update to property ownership $propertyId",
            )
        }
        sendLettingAgentUpdateEmails(propertyId, updatedBullets)
    }

    fun sendUpdateWithLettingAgentRemovedEmails(
        propertyId: Long,
        updatedMessage: String,
        lettingAgentEmail: String,
    ) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
        val actingLandlord = userToLandlordService.getCurrentLandlordForUser()
        val currentUser = currentUserService.getCurrentUserDetails()
        val propertyRecordUrl = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id).toString()

        lettingAgentRemovedEmailService.sendEmail(
            currentUser.email,
            PropertyUpdateWithLettingAgentRemovedConfirmation(
                name = currentUser.name,
                propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                updatedMessage = updatedMessage,
                lettingAgentEmail = lettingAgentEmail,
                propertyRecordUrl = propertyRecordUrl,
            ),
        )
        // TODO: PDJB-1274: Update emails to account for other users in the org

        val otherLandlords = propertyOwnership.landlords.filter { it.id != actingLandlord.id }
        otherLandlords.forEach { landlord ->
            jointLandlordLettingAgentRemovedEmailService.sendEmailToLandlord(
                landlord,
                JointLandlordPropertyUpdateWithLettingAgentRemovedNotification(
                    recipientName = landlord.name,
                    propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                    updatedMessage = updatedMessage,
                    lettingAgentEmail = lettingAgentEmail,
                    propertyRecordUrl = propertyRecordUrl,
                ),
            )
        }
    }
}
