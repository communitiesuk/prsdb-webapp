package uk.gov.communities.prsdb.webapp.examples

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream

@Service
@Profile("local")
class LocalFileUploader : FileUploader {
    override fun uploadFile(
        inputStream: InputStream,
        extension: String?,
    ): String {
        File(".local-uploads").mkdir()
        val extensionWithDot = if (extension != null) ".$extension" else ""
        val destinationRoute = ".local-uploads/destination$extensionWithDot"
        val destinationFile = File(destinationRoute)
        destinationFile.outputStream().use { outputStream ->
            inputStream.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return destinationFile.absolutePath
    }
}
