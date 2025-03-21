package uk.gov.communities.prsdb.webapp.examples

import java.io.InputStream

// TODO PRSD-1001 - ensure this interface is updated to match what is needed for the AWS file upload service
// to allow local to implement it. Do not be lead by what is here in your design - this is currently designed
// to match the example uploader, which just echos the stream to prove it's been read.
interface FileUploader {
    fun uploadFile(
        inputStream: InputStream,
        extension: String?,
    ): String
}
