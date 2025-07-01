package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.File

@Service
@Primary
@Profile("local")
class LocalFileDequarantiner : FileDequarantiner {
    override fun dequarantine(objectKey: String): Boolean {
        File(".local-uploads").mkdir()
        val destinationRoute = ".local-uploads/$objectKey"
        val destinationFile = File(destinationRoute)
        return destinationFile.isFile
    }
}
