package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import java.io.InputStream

interface FileUploader {
    fun uploadFile(
        objectKey: String,
        inputStream: InputStream,
        extension: String,
    ): FileUpload?
}
