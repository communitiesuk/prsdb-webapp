package uk.gov.communities.prsdb.webapp.local.services

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.services.QuarantinedFileDeleter
import java.io.File

@Service
@Primary
@Profile("local")
class LocalQuarantinedFileDeleter : QuarantinedFileDeleter {
    override fun deleteFile(fileUpload: FileUpload): Boolean {
        val localFile = File(".local-uploads/${fileUpload.objectKey}")
        return localFile.exists() && localFile.delete()
    }
}
