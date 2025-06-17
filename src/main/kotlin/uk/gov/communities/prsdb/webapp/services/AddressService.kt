package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.PrsdbService
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.repository.AddressRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@PrsdbService
class AddressService(
    private val addressRepository: AddressRepository,
    private val localAuthorityService: LocalAuthorityService,
) {
    @Transactional
    fun findOrCreateAddress(addressDataModel: AddressDataModel): Address {
        if (addressDataModel.uprn != null) {
            val alreadyExistingAddress = addressRepository.findByUprn(addressDataModel.uprn)
            if (alreadyExistingAddress != null) return alreadyExistingAddress
        }

        val localAuthority =
            addressDataModel.localAuthorityId?.let {
                localAuthorityService.retrieveLocalAuthorityById(it)
            }

        return addressRepository.save(Address(addressDataModel, localAuthority))
    }
}
