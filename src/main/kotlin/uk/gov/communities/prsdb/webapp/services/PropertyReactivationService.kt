package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID

@PrsdbWebService
class PropertyReactivationService(
    private val activeInactiveService: ActiveInactiveService,
    private val addressAvailabilityService: AddressAvailabilityService,
    private val userToLandlordService: UserToLandlordService,
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

        val landlordReactivated = !userToLandlordService.getCurrentLandlordForUser().isActive

        activeInactiveService.reactivateProperty(propertyOwnershipId)
        session.setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
        session.setAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION, landlordReactivated)
    }

    fun getLastPropertyOwnershipIdReactivatedThisSession(): Long? = session.getAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID) as? Long

    fun wasLandlordReactivatedByLastReactivation(): Boolean =
        session.getAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION) == true
}
