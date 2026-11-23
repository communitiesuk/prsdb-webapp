package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyReactivationConfirmationEmail

@PrsdbWebService
class PropertyReactivationEmailService(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val currentUserService: CurrentUserService,
    private val userToLandlordService: UserToLandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailSender: EmailNotificationService<PropertyReactivationConfirmationEmail>,
) {
    // TODO PDJB-2050: Notify joint landlords that the property has been reactivated
    fun sendConfirmationEmail(
        propertyOwnershipId: Long,
        landlordReactivated: Boolean,
    ) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)
        val currentUser = currentUserService.getRegisteredUserDetails()
        val hasJointLandlords = propertyOwnership.otherLandlordsTo(userToLandlordService.getCurrentLandlordForUser()).isNotEmpty()

        // TODO PDJB-1274: Update emails to account for other users in the org
        confirmationEmailSender.sendEmail(
            currentUser.email,
            PropertyReactivationConfirmationEmail(
                recipientName = currentUser.name,
                multiLineAddress = propertyOwnership.address.toMultiLineAddress(),
                registrationNumber = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
                dashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                hasJointLandlords = hasJointLandlords,
                landlordReactivated = landlordReactivated,
                // TODO PDJB-2076: Include the payment details when payment is taken during reactivation
                paymentDetails = null,
            ),
        )
    }
}
