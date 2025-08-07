package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import java.io.InputStream

@PrsdbWebService
class UploadService(
    private val uploader: FileUploader,
    private val downloader: FileDownloader,
    private val uploadRepository: FileUploadRepository,
) {
    fun uploadFile(
        objectKey: String,
        inputStream: InputStream,
        extension: String,
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
}
