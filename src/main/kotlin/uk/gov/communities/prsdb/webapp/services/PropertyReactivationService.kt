package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper
import uk.gov.communities.prsdb.webapp.helpers.TransactionHelper.Companion.runAfterTransactionCommits
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel

@PrsdbWebService
class PropertyReactivationService(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyComplianceService: PropertyComplianceService,
    private val activationService: ActivationService,
    private val addressAvailabilityService: AddressAvailabilityService,
    private val landlordActiveStatusService: LandlordActiveStatusService,
    private val propertyReactivationEmailService: PropertyReactivationEmailService,
    private val session: HttpSession,
    private val userToLandlordService: UserToLandlordService,
) {
    @Transactional
    fun reactivatePropertyIfAddressAvailable(
        propertyOwnershipId: Long,
        uprn: Long?,
        reactivationData: PropertyReactivationDataModel,
    ) {
        if (uprn != null && addressAvailabilityService.isAddressOwned(uprn)) {
            throw EntityExistsException(
                "Cannot reactivate property ownership $propertyOwnershipId as its address (UPRN $uprn) " +
                    "is already registered to an active property ownership",
            )
        }

        val landlordWillBeReactivated = landlordActiveStatusService.isCurrentLandlordInactive()

        saveReactivationData(propertyOwnershipId, reactivationData)
        activationService.reactivateProperty(propertyOwnershipId, userToLandlordService.getCurrentLandlordForUser())
        runAfterTransactionCommits {
            session.setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
            session.setAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION, landlordWillBeReactivated)

            NotificationHelper.sendNotificationAndLogFailures(
                "the property reactivation confirmation email for property ownership $propertyOwnershipId",
            ) {
                propertyReactivationEmailService.sendConfirmationEmail(propertyOwnershipId, landlordWillBeReactivated)
            }

            NotificationHelper.sendNotificationAndLogFailures(
                "the joint landlord notifications for the reactivation of property ownership $propertyOwnershipId",
            ) {
                propertyReactivationEmailService.sendJointLandlordNotifications(propertyOwnershipId)
            }
        }
    }

    fun getLastPropertyOwnershipIdReactivatedThisSession(): Long? = session.getAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID) as? Long

    fun wasLandlordReactivatedByLastReactivation(): Boolean =
        session.getAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION) == true

    private fun saveReactivationData(
        propertyOwnershipId: Long,
        reactivationData: PropertyReactivationDataModel,
    ) {
        val propertyOwnership = propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId)
        reactivationData.licensing?.let { propertyOwnershipService.saveReactivationLicensing(propertyOwnership, it) }
        reactivationData.tenancyDetails?.let { propertyOwnershipService.saveReactivationTenancyDetails(propertyOwnership, it) }

        if (reactivationData.gasSafety != null || reactivationData.electricalSafety != null || reactivationData.epc != null) {
            propertyComplianceService.saveReactivationComplianceData(
                propertyOwnership,
                reactivationData.gasSafety,
                reactivationData.electricalSafety,
                reactivationData.epc,
            )
        }
    }
}
