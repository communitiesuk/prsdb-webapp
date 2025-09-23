package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.repository.NgdAddressRepository

@PrsdbWebService
class NgdAddressService(
    private val ngdAddressRepository: NgdAddressRepository,
) {
    fun getStoredDataPackageVersionId(): String? {
        val tableComment = ngdAddressRepository.findComment() ?: return null
        val dataPackageVersionId = tableComment.removePrefix(DATA_PACKAGE_VERSION_COMMENT_PREFIX)
        return dataPackageVersionId.ifEmpty { null }
    }

    fun setStoredDataPackageVersionId(dataPackageVersionId: String) {
        val comment = "$DATA_PACKAGE_VERSION_COMMENT_PREFIX$dataPackageVersionId"
        ngdAddressRepository.saveComment(comment)
    }

    companion object {
        const val DATA_PACKAGE_VERSION_COMMENT_PREFIX = "dataPackageVersionId="
    }
}
