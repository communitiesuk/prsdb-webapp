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
    fun createProperty(
        addressDataModel: AddressDataModel,
        propertyType: PropertyType,
        isActive: Boolean = true,
        registrationStatus: RegistrationStatus = RegistrationStatus.REGISTERED,
    ): Property {
        val address = addressService.findOrCreateAddress(addressDataModel)

        return propertyRepository.save(
            Property(
                status = registrationStatus,
                isActive = isActive,
                propertyType = propertyType,
                address = address,
            ),
        )
    }
}
