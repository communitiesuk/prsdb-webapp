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
    fun createAddress(addressDataModel: AddressDataModel): Address {
        if (addressDataModel.uprn != null) {
            val alreadyExistingAddress = addressRepository.findByUprn(addressDataModel.uprn)
            if (alreadyExistingAddress != null) return alreadyExistingAddress
        }

        return addressRepository.save(Address(addressDataModel))
    }

    fun createAddress(
        addressLineOne: String,
        townOrCity: String,
        postcode: String,
        addressLineTwo: String? = null,
        county: String? = null,
    ): Address {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress =
                    listOfNotNull(addressLineOne, addressLineTwo, townOrCity, postcode, county)
                        .joinToString(", "),
                townName = townOrCity,
                postcode = postcode,
            )

        return addressRepository.save(Address(addressDataModel))
    }
}