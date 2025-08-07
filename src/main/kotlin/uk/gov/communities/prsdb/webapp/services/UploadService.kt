package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import java.io.InputStream

@PrsdbWebService
class UploadService(
    private val uploader: FileUploader,
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
}
