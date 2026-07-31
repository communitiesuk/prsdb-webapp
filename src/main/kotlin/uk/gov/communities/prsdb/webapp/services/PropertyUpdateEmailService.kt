package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordPropertyUpdateNotificationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation

@PrsdbWebService
class PropertyUpdateEmailService(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val userToLandlordService: UserToLandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailService: EmailNotificationService<PropertyUpdateConfirmation>,
    private val notificationEmailService: EmailNotificationService<JointLandlordPropertyUpdateNotificationEmail>,
) {
    fun sendUpdateEmails(
        propertyId: Long,
        updatedBullets: List<String>,
    ) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
        val actingLandlord = userToLandlordService.getCurrentLandlordForUser()
        val registrationNumber =
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString()

        // TODO: PDJB-1274: Update emails to account for org landlord
        confirmationEmailService.sendEmail(
            actingLandlord.email,
            PropertyUpdateConfirmation(
                singleLineAddress = propertyOwnership.address.singleLineAddress,
                registrationNumber = registrationNumber,
                updatedBullets = updatedBullets,
                dashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri(),
            ),
        )

        val otherLandlords = propertyOwnership.landlords.filter { it.id != actingLandlord.id }
        if (otherLandlords.isNotEmpty()) {
            val propertyRecordUrl = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id).toString()
            // TODO: PDJB-1274: Update emails to account for org landlord
            otherLandlords.forEach { landlord ->
                notificationEmailService.sendEmail(
                    landlord.email,
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
}
