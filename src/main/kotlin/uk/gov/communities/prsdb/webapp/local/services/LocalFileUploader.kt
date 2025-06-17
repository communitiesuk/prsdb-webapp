package uk.gov.communities.prsdb.webapp.local.services

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import uk.gov.communities.prsdb.webapp.annotations.PrsdbService
import uk.gov.communities.prsdb.webapp.services.FileUploader
import java.io.File
import java.io.InputStream

@PrsdbService
@Primary
@Profile("local")
class LocalFileUploader : FileUploader {
    private val forbiddenFilenameCharacters = listOf(':', '<', '>', '"', '?', '*', '&', '/', '\\', ',')

    override fun uploadFile(
        objectKey: String,
        inputStream: InputStream,
    ): Boolean {
        val cleanObjectKey =
            objectKey
                .map { char -> if (char in forbiddenFilenameCharacters) "" else char }
                .joinToString("")
        File(".local-uploads").mkdir()
        val destinationRoute = ".local-uploads/$cleanObjectKey"
        val destinationFile = File(destinationRoute)
        destinationFile.outputStream().use { outputStream ->
            inputStream.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return true
    }
}
