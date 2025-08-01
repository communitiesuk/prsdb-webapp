package uk.gov.communities.prsdb.webapp.local.services

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.services.FileUploader
import java.io.File
import java.io.InputStream

@PrsdbWebService
@Primary
@Profile("local")
class LocalFileUploader(
    private val uploadRepository: FileUploadRepository,
) : FileUploader {
    private val forbiddenFilenameCharacters = listOf(':', '<', '>', '"', '?', '*', '&', '/', '\\', ',')

    override fun uploadFile(
        objectKey: String,
        inputStream: InputStream,
    ): FileUpload? {
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

        return uploadRepository.save(
            FileUpload(
                status = FileUploadStatus.SCANNED,
                s3Key = objectKey,
            ),
        )
    }
}
