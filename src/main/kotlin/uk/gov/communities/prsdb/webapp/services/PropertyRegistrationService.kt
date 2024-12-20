package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository

@Service
class PropertyRegistrationService(
    private val propertyRepository: PropertyRepository,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val addressDataService: AddressDataService,
) {
    fun getIsAddressRegistered(uprn: Long): Boolean {
        val cachedResult = addressDataService.getCachedAddressRegisteredResult(uprn)
        if (cachedResult != null) return cachedResult

        val property = propertyRepository.findByAddress_Uprn(uprn)
        if (property == null || !property.isActive) {
            addressDataService.setCachedAddressRegisteredResult(uprn, false)
            return false
        }

        val databaseResult = propertyOwnershipRepository.existsByIsActiveTrueAndProperty_Id(property.id)
        addressDataService.setCachedAddressRegisteredResult(uprn, databaseResult)
        return databaseResult
    }
}
