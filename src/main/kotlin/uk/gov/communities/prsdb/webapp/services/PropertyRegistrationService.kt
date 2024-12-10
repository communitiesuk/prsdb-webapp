package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
class PropertyRegistrationService(
    private val propertyRepository: PropertyRepository,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
) {
    fun getIsAddressRegistered(uprn: Long): Boolean {
        val property = propertyRepository.findByAddress_Uprn(uprn)
        if (property == null || !property.isActive || property.id == null) return false
        val propertyOwnerships = propertyOwnershipRepository.findByProperty_Id(property.id)
        return (propertyOwnerships.any({ it.isActive }))
    }
}
