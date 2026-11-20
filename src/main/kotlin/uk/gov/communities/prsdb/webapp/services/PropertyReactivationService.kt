package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyReactivationConfirmationEmail

@PrsdbWebService
class PropertyReactivationService(
    private val activeInactiveService: ActiveInactiveService,
    private val addressAvailabilityService: AddressAvailabilityService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val currentUserService: CurrentUserService,
    private val userToLandlordService: UserToLandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val confirmationEmailSender: EmailNotificationService<PropertyReactivationConfirmationEmail>,
    private val session: HttpSession,
) {
    @Transactional
    fun reactivatePropertyIfAddressAvailable(
        propertyOwnershipId: Long,
        uprn: Long?,
    ) {
        if (uprn != null && addressAvailabilityService.isAddressOwned(uprn)) {
            throw EntityExistsException(
                "Cannot reactivate property ownership $propertyOwnershipId as its address (UPRN $uprn) " +
                    "is already registered to an active property ownership",
            )
        }

        val landlordWillBeReactivated = activeInactiveService.isCurrentLandlordInactive()

        activeInactiveService.reactivateProperty(propertyOwnershipId)
        session.setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)

        // TODO PDJB-2050: Notify joint landlords that the property has been reactivated
        NotificationHelper.sendNotificationAndLogFailures(
            "the property reactivation confirmation email for property ownership $propertyOwnershipId",
        ) {
            sendConfirmationEmail(propertyOwnershipId, landlordWillBeReactivated)
        }
    }

    fun getLastPropertyOwnershipIdReactivatedThisSession(): Long? = session.getAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID) as? Long

    private fun sendConfirmationEmail(
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
