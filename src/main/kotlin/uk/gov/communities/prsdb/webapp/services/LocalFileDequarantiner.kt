package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.File

@Service
@Primary
@Profile("local")
class LocalFileDequarantiner : FileDequarantiner {
    override fun dequarantineFile(objectKey: String): Boolean {
        val localFile = File(".local-uploads/$objectKey")
        return localFile.exists()
    }

    override fun deleteFile(objectKey: String): Boolean {
        val localFile = File(".local-uploads/$objectKey")
        return if (localFile.exists()) {
            localFile.delete()
        } else {
            false
        }
    }
}
