package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository

@Service
class UploadDequarantiner(
    private val dequarantiningFileCopier: DequarantiningFileCopier,
    private val quarantinedFileDeleter: QuarantinedFileDeleter,
    private val fileUploadRepository: FileUploadRepository,
) {
    fun dequarantineFile(fileUpload: FileUpload): Boolean {
        val transferResult = dequarantiningFileCopier.copyFile(fileUpload)
        return if (transferResult != null && quarantinedFileDeleter.deleteFile(fileUpload)) {
            fileUpload.eTag = transferResult.eTag
            fileUpload.versionId = transferResult.versionId
            fileUpload.status = FileUploadStatus.SCANNED

            fileUploadRepository.save(fileUpload)
            true
        } else {
            false
        }
    }

    fun deleteQuarantinedFile(fileUpload: FileUpload): Boolean {
        if (quarantinedFileDeleter.deleteFile(fileUpload)) {
            fileUpload.status = FileUploadStatus.DELETED
            fileUploadRepository.save(fileUpload)
            return true
        } else {
            return false
        }
    }
}
