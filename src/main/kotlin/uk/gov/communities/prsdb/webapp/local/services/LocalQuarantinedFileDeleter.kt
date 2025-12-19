package uk.gov.communities.prsdb.webapp.local.services

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import uk.gov.communities.prsdb.webapp.annotations.processAnnotations.PrsdbProcessService
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.services.QuarantinedFileDeleter
import java.io.File

@PrsdbProcessService
@Primary
@Profile("local")
class LocalQuarantinedFileDeleter : QuarantinedFileDeleter {
    override fun deleteFile(fileUpload: FileUpload): Boolean {
        val localFile = File(".local-uploads/${fileUpload.objectKey}")
        return localFile.exists() && localFile.delete()
    }
}
