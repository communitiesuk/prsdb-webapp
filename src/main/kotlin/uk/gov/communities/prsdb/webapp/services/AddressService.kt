package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.repository.AddressRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@PrsdbWebService
class AddressService(
    private val addressRepository: AddressRepository,
    private val localAuthorityService: LocalAuthorityService,
) {
    @Transactional
    fun findOrCreateAddress(addressDataModel: AddressDataModel) =
        if (addressDataModel.uprn != null) {
            addressRepository.findByIsActiveTrueAndUprn(addressDataModel.uprn)
                ?: throw EntityNotFoundException("No active address found with UPRN ${addressDataModel.uprn}")
        } else {
            val localAuthority = addressDataModel.localAuthorityId?.let { localAuthorityService.retrieveLocalAuthorityById(it) }
            addressRepository.save(Address(addressDataModel, localAuthority))
        }

    fun searchForAddresses(
        houseNameOrNumber: String,
        postcode: String,
        restrictToEngland: Boolean = false,
    ) = addressRepository.search(houseNameOrNumber, postcode, restrictToEngland).map { AddressDataModel.fromAddress(it) }
}
