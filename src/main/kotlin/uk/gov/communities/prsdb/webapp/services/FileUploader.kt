package uk.gov.communities.prsdb.webapp.services

import java.io.InputStream

interface FileUploader {
    fun uploadFile(
        objectKey: String,
        inputStream: InputStream,
    ): Boolean
}
