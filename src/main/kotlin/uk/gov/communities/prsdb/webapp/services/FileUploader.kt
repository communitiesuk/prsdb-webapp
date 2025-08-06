package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.models.dataModels.FileUploadResult
import java.io.InputStream

interface FileUploader {
    fun uploadFile(
        objectKey: String,
        inputStream: InputStream,
    ): FileUploadResult?
}
