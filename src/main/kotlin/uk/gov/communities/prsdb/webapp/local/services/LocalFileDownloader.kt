package uk.gov.communities.prsdb.webapp.local.services

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.services.FileDownloader
import java.io.File

@PrsdbWebService
@Primary
@Profile("local")
class LocalFileDownloader : FileDownloader {
    override fun getDownloadUrl(
        fileUpload: FileUpload,
        fileName: String?,
    ): String {
        if (!isFileDownloadable(fileUpload)) {
            throw PrsdbWebException(
                "File with object key ${fileUpload.objectKey} is not downloadable. " +
                    "Status: ${fileUpload.status}",
            )
        }

        val destinationFile = File(".local-uploads/safe/${fileUpload.objectKey}")

        if (!destinationFile.isFile) {
            throw PrsdbWebException(
                "File with object key ${fileUpload.objectKey} does not exist in the local storage.",
            )
        }
        val contentType =
            when (fileUpload.extension) {
                "pdf" -> "application/pdf"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                else -> "application/octet-stream"
            }

        return if (fileName != null) {
            "/local-file/safe/${fileUpload.objectKey}?fileName=$fileName&contentType=$contentType"
        } else {
            "/local-file/safe/${fileUpload.objectKey}?fileName=${fileUpload.objectKey}&contentType=$contentType"
        }
    }
}
