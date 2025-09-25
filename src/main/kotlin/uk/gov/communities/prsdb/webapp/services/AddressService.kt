package uk.gov.communities.prsdb.webapp.services

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

    fun getStoredDataPackageVersionId(): String? {
        val tableComment = addressRepository.findComment() ?: return null
        val dataPackageVersionId = tableComment.removePrefix(DATA_PACKAGE_VERSION_COMMENT_PREFIX)
        return dataPackageVersionId.ifEmpty { null }
    }

    fun setStoredDataPackageVersionId(dataPackageVersionId: String) {
        val comment = "$DATA_PACKAGE_VERSION_COMMENT_PREFIX$dataPackageVersionId"
        addressRepository.saveComment(comment)
    }

    companion object {
        const val DATA_PACKAGE_VERSION_COMMENT_PREFIX = "dataPackageVersionId="
    }
}
