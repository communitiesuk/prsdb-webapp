package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.AddressSearchRegion
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.repository.AddressRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@PrsdbWebService
class AddressService(
    private val addressRepository: AddressRepository,
    private val localCouncilService: LocalCouncilService,
) {
    @Transactional
    fun findOrCreateAddress(addressDataModel: AddressDataModel) =
        if (addressDataModel.uprn != null) {
            addressRepository.findByIsActiveTrueAndUprn(addressDataModel.uprn)
                ?: throw EntityNotFoundException("No active address found with UPRN ${addressDataModel.uprn}")
        } else {
            val localCouncil = addressDataModel.localCouncilId?.let { localCouncilService.retrieveLocalCouncilById(it) }
            addressRepository.save(Address(addressDataModel, localCouncil))
        }

    fun searchForAddresses(
        houseNameOrNumber: String,
        postcode: String,
        region: AddressSearchRegion = AddressSearchRegion.UK,
    ): List<AddressDataModel> {
        val addresses =
            when (region) {
                AddressSearchRegion.WITH_ENGLISH_LOCAL_COUNCIL -> {
                    addressRepository.searchWithEnglishLocalCouncil(houseNameOrNumber, postcode)
                }

                AddressSearchRegion.ENGLAND_AND_WALES -> {
                    addressRepository.searchInEnglandAndWales(houseNameOrNumber, postcode)
                }

                AddressSearchRegion.UK -> {
                    addressRepository.searchInUk(houseNameOrNumber, postcode)
                }
            }
        return addresses.map { AddressDataModel.fromAddress(it) }
    }
}
