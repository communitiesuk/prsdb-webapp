package uk.gov.communities.prsdb.webapp.examples

import java.io.InputStream

interface FileUploader {
    fun uploadFile(
        objectKey: String,
        inputStream: InputStream,
    ): Boolean
}
