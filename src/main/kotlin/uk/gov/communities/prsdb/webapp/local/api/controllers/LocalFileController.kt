package uk.gov.communities.prsdb.webapp.local.api.controllers

import org.springframework.context.annotation.Profile
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import java.io.File

@Profile("local")
@PrsdbController
class LocalFileController {
    @GetMapping("/local-file/{*fileRoute}")
    fun getLocalFile(
        @PathVariable("fileRoute") fileRoute: String,
        @RequestParam("contentType") contentType: String,
        @RequestParam("fileName") fileName: String,
    ): ResponseEntity<Resource> {
        val file = File(".local-uploads/$fileRoute")

        if (!file.exists()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "File not found: $fileRoute",
            )
        }

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.CONTENT_TYPE, contentType)
            .body(FileSystemResource(file))
    }
}
