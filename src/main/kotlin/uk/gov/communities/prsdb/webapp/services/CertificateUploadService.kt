package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.FileCategory
import uk.gov.communities.prsdb.webapp.database.entity.CertificateUpload
import uk.gov.communities.prsdb.webapp.database.repository.CertificateUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository

@PrsdbWebService
class CertificateUploadService(
    private val certificateUploadRepository: CertificateUploadRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
) {
    fun saveCertificateUpload(
        propertyOwnershipId: Long,
        fileUploadId: Long,
        certificateType: FileCategory,
    ): CertificateUpload {
        val propertyOwnership = propertyOwnershipRepository.getReferenceById(propertyOwnershipId)
        val fileUpload = fileUploadRepository.getReferenceById(fileUploadId)

        return certificateUploadRepository.save(
            CertificateUpload(
                upload = fileUpload,
                category = certificateType,
                propertyOwnership = propertyOwnership,
            ),
        )
    }
}
