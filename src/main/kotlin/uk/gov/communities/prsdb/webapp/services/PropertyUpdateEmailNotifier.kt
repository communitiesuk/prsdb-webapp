package uk.gov.communities.prsdb.webapp.services

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordPropertyUpdateNotificationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation

@PrsdbWebService
class PropertyUpdateEmailNotifier(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val landlordService: LandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailService: EmailNotificationService<PropertyUpdateConfirmation>,
    private val notificationEmailService: EmailNotificationService<JointLandlordPropertyUpdateNotificationEmail>,
) {
    fun sendUpdateEmails(
        propertyId: Long,
        updatedBullets: List<String>,
    ) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
        val actingLandlord = getActingLandlord()
        val registrationNumber =
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString()

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

    private fun getActingLandlord(): Landlord {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        return landlordService.retrieveLandlordByBaseUserId(baseUserId)
            ?: throw PrsdbWebException("Landlord record not found for logged in user with baseUserId $baseUserId")
    }
}
