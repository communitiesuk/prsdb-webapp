package uk.gov.communities.prsdb.webapp.local.services

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator
import uk.gov.communities.prsdb.webapp.services.DequarantiningFileCopier
import java.io.File

@Service
@Primary
@Profile("local")
class LocalDequarantiningFileCopier : DequarantiningFileCopier {
    override fun copyFile(fileUpload: FileUpload): UploadedFileLocator? {
        if (File(".local-uploads/${fileUpload.objectKey}").isFile) {
            File(".local-uploads/safe").mkdir()
            val destinationFile = File(".local-uploads/safe/${fileUpload.objectKey}")
            destinationFile.outputStream().use { outputStream ->
                File(".local-uploads/${fileUpload.objectKey}").inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return UploadedFileLocator(
                objectKey = fileUpload.objectKey,
                eTag = fileUpload.objectKey,
                versionId = fileUpload.objectKey,
            )
        } else {
            return null
        }
    }
}
