package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import java.io.InputStream

@PrsdbWebService
class UploadService(
    private val uploader: FileUploader,
    private val downloader: FileDownloader,
    private val safeFileDeleter: SafeFileDeleter,
    private val uploadRepository: FileUploadRepository,
) {
    fun uploadFile(
        objectKey: String,
        inputStream: InputStream,
        extension: String,
        fileName: String? = null,
    ): FileUpload? {
        val uploadResult =
            uploader.uploadFile(objectKey, inputStream)
                ?: return null

        val fileUpload =
            FileUpload(
                objectKey = uploadResult.objectKey,
                eTag = uploadResult.eTag,
                versionId = uploadResult.versionId,
                extension = extension,
                status = FileUploadStatus.QUARANTINED,
                fileName = fileName,
            )

        return uploadRepository.save(fileUpload)
    }

    fun getFileUploadById(fileUploadId: Long): FileUpload =
        uploadRepository.findById(fileUploadId).orElseThrow {
            IllegalArgumentException("File upload with ID $fileUploadId not found")
        }

    fun getDownloadUrl(
        fileUpload: FileUpload,
        fileName: String? = null,
    ): String = downloader.getDownloadUrl(fileUpload, fileName)

    fun getDownloadUrlOrNull(
        fileUpload: FileUpload,
        fileName: String? = null,
    ): String? =
        if (downloader.isFileDownloadable(fileUpload)) {
            downloader.getDownloadUrl(fileUpload, fileName)
        } else {
            null
        }

    @Transactional
    fun deleteUploadedFile(fileUploadId: Long) {
        val fileUpload = getFileUploadById(fileUploadId)

        if (fileUpload.status == FileUploadStatus.DELETED) return

        val previousStatus = fileUpload.status
        fileUpload.status = FileUploadStatus.DELETED
        uploadRepository.save(fileUpload)

        if (previousStatus == FileUploadStatus.SCANNED) {
            safeFileDeleter.deleteFile(fileUpload)
        }
    }
}
