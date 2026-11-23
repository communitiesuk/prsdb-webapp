package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.helpers.TransactionHelper.Companion.runAfterTransactionCommits
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel

@PrsdbWebService
class PropertyReactivationService(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyComplianceService: PropertyComplianceService,
    private val activeInactiveService: ActiveInactiveService,
    private val addressAvailabilityService: AddressAvailabilityService,
    private val userToLandlordService: UserToLandlordService,
    private val session: HttpSession,
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

        val landlordReactivated = !userToLandlordService.getCurrentLandlordForUser().isActive

        saveReactivationData(propertyOwnershipId, reactivationData)
        activeInactiveService.reactivateProperty(propertyOwnershipId)
        runAfterTransactionCommits {
            session.setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
            session.setAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION, landlordReactivated)
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
