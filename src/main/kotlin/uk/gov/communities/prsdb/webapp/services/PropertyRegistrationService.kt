package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository

@Service
class PropertyRegistrationService(
    private val propertyRepository: PropertyRepository,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
) {
    fun getIsAddressRegistered(uprn: Long): Boolean {
        val property = propertyRepository.findByAddress_Uprn(uprn)
        if (property == null || !property.isActive || property.id == null) return false
        val propertyOwnership = propertyOwnershipRepository.findByIsActiveTrueAndProperty_Id(property.id)
        return (propertyOwnership != null)
    }
}
