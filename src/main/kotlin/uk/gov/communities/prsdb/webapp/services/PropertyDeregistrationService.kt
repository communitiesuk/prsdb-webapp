package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DEREGISTRATION_ENTITY_IDS

@Service
class PropertyDeregistrationService(
    private val propertyService: PropertyService,
    private val licenseService: LicenseService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val session: HttpSession,
) {
    fun deregisterProperty(propertyOwnershipId: Long) {
        propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)?.let {
            propertyOwnershipService.deletePropertyOwnership(it)
            propertyService.deleteProperty(it.property)
            licenseService.deleteLicense(it.license)
        }
    }

    fun addDeregisteredPropertyAndOwnershipIdsToSession(
        propertyOwnershipId: Long,
        propertyId: Long,
    ) = session.setAttribute(
        PROPERTY_DEREGISTRATION_ENTITY_IDS,
        getDeregisteredPropertyAndOwnershipIdsFromSession().plus(Pair(propertyOwnershipId, propertyId)),
    )

    fun getDeregisteredPropertyAndOwnershipIdsFromSession(): MutableList<Pair<Long, Long>> =
        session.getAttribute(PROPERTY_DEREGISTRATION_ENTITY_IDS) as MutableList<Pair<Long, Long>>?
            ?: mutableListOf()
}
