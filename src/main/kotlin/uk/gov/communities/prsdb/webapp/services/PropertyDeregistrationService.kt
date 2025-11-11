package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_DEREGISTERED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

@PrsdbWebService
class PropertyDeregistrationService(
    private val licenseService: LicenseService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyComplianceService: PropertyComplianceService,
    private val formContextService: FormContextService,
    private val session: HttpSession,
) {
    @Transactional
    fun deregisterProperty(propertyOwnershipId: Long) {
        propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)?.let {
            propertyComplianceService.deletePropertyComplianceByOwnershipId(it.id)
            it.incompleteComplianceForm?.let { incompleteComplianceForm -> formContextService.deleteFormContext(incompleteComplianceForm) }
            propertyOwnershipService.deletePropertyOwnership(it)
            if (it.license != null) licenseService.deleteLicense(it.license!!)
        }
    }

    @Transactional
    fun deregisterProperties(propertyOwnerships: List<PropertyOwnership>) {
        val propertyOwnershipIds = propertyOwnerships.map { it.id }
        val licenses = propertyOwnerships.mapNotNull { it.license }
        val incompleteComplianceForms = propertyOwnerships.mapNotNull { it.incompleteComplianceForm }

        propertyComplianceService.deletePropertyCompliancesByOwnershipIds(propertyOwnershipIds)
        propertyOwnershipService.deletePropertyOwnerships(propertyOwnerships)
        licenseService.deleteLicenses(licenses)
        formContextService.deleteFormContexts(incompleteComplianceForms)
    }

    fun addDeregisteredPropertyOwnershipIdToSession(propertyOwnershipId: Long) =
        session.setAttribute(
            PROPERTIES_DEREGISTERED_THIS_SESSION,
            getDeregisteredPropertyOwnershipIdsFromSession() + propertyOwnershipId,
        )

    @Suppress("UNCHECKED_CAST")
    fun getDeregisteredPropertyOwnershipIdsFromSession(): MutableList<Long> =
        session.getAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION) as MutableList<Long>?
            ?: mutableListOf()
}
