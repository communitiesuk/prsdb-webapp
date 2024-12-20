package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.repository.AddressRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@Service
class AddressService(
    private val addressRepository: AddressRepository,
) {
    @Transactional
    fun findOrCreateAddress(addressDataModel: AddressDataModel): Address {
        if (addressDataModel.uprn != null) {
            val alreadyExistingAddress = addressRepository.findByUprn(addressDataModel.uprn)
            if (alreadyExistingAddress != null) return alreadyExistingAddress
        }

        return addressRepository.save(Address(addressDataModel))
    }
}
