package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@Service
class PropertyService(
    private val propertyRepository: PropertyRepository,
    private val addressService: AddressService,
) {
    @Transactional
    fun activateOrCreateProperty(
        addressDataModel: AddressDataModel,
        propertyType: PropertyType,
    ): Property {
        val address = addressService.findOrCreateAddress(addressDataModel)

        val existingProperty = address.uprn?.let { propertyRepository.findByAddress_Uprn(it) }

        return if (existingProperty != null) {
            activateProperty(existingProperty, propertyType)
        } else {
            propertyRepository.save(
                Property(
                    status = RegistrationStatus.REGISTERED,
                    propertyType = propertyType,
                    address = address,
                ),
            )
        }
    }

    fun activateProperty(
        property: Property,
        propertyType: PropertyType,
    ): Property {
        property.isActive = true
        property.propertyBuildType = propertyType
        return propertyRepository.save(property)
    }

    fun deleteProperty(property: Property) {
        propertyRepository.delete(property)
    }
}
