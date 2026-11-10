package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.helpers.TransactionHelper.Companion.runAfterTransactionCommits

@PrsdbTaskService
class UploadDequarantiner(
    private val dequarantiningFileCopier: DequarantiningFileCopier,
    private val quarantinedFileDeleter: QuarantinedFileDeleter,
    private val fileUploadRepository: FileUploadRepository,
) {
    fun dequarantineFile(fileUpload: FileUpload): Boolean {
        val transferResult = dequarantiningFileCopier.copyFile(fileUpload) ?: return false

        fileUpload.eTag = transferResult.eTag
        fileUpload.versionId = transferResult.versionId
        fileUpload.status = FileUploadStatus.SCANNED
        fileUploadRepository.save(fileUpload)

        runAfterTransactionCommits { deleteDequarantinedOriginal(fileUpload) }
        return true
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

    private fun deleteDequarantinedOriginal(fileUpload: FileUpload) {
        try {
            if (!quarantinedFileDeleter.deleteFile(fileUpload)) {
                println("Failed to delete quarantined file after dequarantining it: ${fileUpload.objectKey}")
            }
        } catch (exception: Exception) {
            println(
                "Failed to delete quarantined file after dequarantining it: ${fileUpload.objectKey}: " +
                    exception.stackTraceToString(),
            )
        }
    }
}
