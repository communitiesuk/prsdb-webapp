package uk.gov.communities.prsdb.webapp.examples

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream

@Service
@Primary
@Profile("local")
class LocalFileUploader : FileUploader {
    private val forbiddenFilenameCharacters = listOf(':', '<', '>', '"', '?', '*', '&')

    override fun uploadFile(
        objectKey: String,
        inputStream: InputStream,
        streamSize: Long,
    ): String {
        val cleanObjectKey =
            objectKey
                .map { char -> if (char in forbiddenFilenameCharacters) "" else char }
                .joinToString("")
        File(".local-uploads/${cleanObjectKey.substring(0, cleanObjectKey.lastIndexOf('/'))}").mkdirs()
        val destinationRoute = ".local-uploads/$cleanObjectKey"
        val destinationFile = File(destinationRoute)
        destinationFile.outputStream().use { outputStream ->
            inputStream.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return destinationFile.absolutePath
    }
}
