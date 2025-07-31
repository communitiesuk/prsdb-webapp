package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import java.io.File

@Service
@Primary
@Profile("local")
class LocalFileDequarantiner(
    private val fileUploadRepository: FileUploadRepository,
) : FileDequarantiner {
    override fun dequarantineFile(fileUpload: FileUpload): Boolean {
        if (File(".local-uploads/${fileUpload.objectKey}").isFile) {
            fileUpload.status = FileUploadStatus.SCANNED
            fileUploadRepository.save(fileUpload)
            return true
        } else {
            return false
        }
    }

    override fun deleteFile(fileUpload: FileUpload): Boolean {
        val localFile = File(".local-uploads/${fileUpload.objectKey}")
        return if (localFile.exists() && localFile.delete()) {
            fileUploadRepository.delete(fileUpload)
            true
        } else {
            false
        }
    }
}
