package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DEREGISTRATION_ENTITY_IDS
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

@PrsdbWebService
class PropertyDeregistrationService(
    private val propertyService: PropertyService,
    private val licenseService: LicenseService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyComplianceService: PropertyComplianceService,
    private val session: HttpSession,
) {
    @Transactional
    fun deregisterProperty(propertyOwnershipId: Long) {
        propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)?.let {
            propertyComplianceService.deletePropertyComplianceIfExists(it.id)
            propertyOwnershipService.deletePropertyOwnership(it)
            propertyService.deleteProperty(it.property)
            if (it.license != null) licenseService.deleteLicense(it.license!!)
        }
    }

    @Transactional
    fun deregisterProperties(propertyOwnerships: List<PropertyOwnership>) {
        val properties = propertyOwnerships.map { it.property }
        val licenses = propertyOwnerships.mapNotNull { it.license }
        val compliances = propertyComplianceService.getPropertyCompliancesForPropertyOwnerships(propertyOwnerships)

        if (compliances.isNotEmpty()) propertyComplianceService.deletePropertyCompliances(compliances)
        propertyOwnershipService.deletePropertyOwnerships(propertyOwnerships)
        propertyService.deleteProperties(properties)
        licenseService.deleteLicenses(licenses)
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
