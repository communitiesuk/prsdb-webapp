package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.PropertyManager
import uk.gov.communities.prsdb.webapp.database.repository.PropertyManagerRepository

@PrsdbWebService
class PropertyManagerService(
    private val propertyManagerRepository: PropertyManagerRepository,
) {
    fun createPropertyManager(
        name: String,
        email: String,
    ): PropertyManager = propertyManagerRepository.save(PropertyManager(name, email))

    // A null name means the landlord does not use a property manager, which deletes any existing row.
    // The caller assigns the result to propertyOwnership.propertyManager.
    fun updatePropertyManager(
        propertyManager: PropertyManager?,
        name: String?,
        email: String?,
    ): PropertyManager? =
        if (name == null) {
            propertyManager?.let { propertyManagerRepository.delete(it) }
            null
        } else if (propertyManager == null) {
            createPropertyManager(name, email!!)
        } else {
            propertyManager.name = name
            propertyManager.email = email!!
            propertyManagerRepository.save(propertyManager)
            propertyManager
        }
}
