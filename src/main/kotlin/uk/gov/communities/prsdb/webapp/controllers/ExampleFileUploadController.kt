package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping("example/file-upload")
class ExampleFileUploadController {
    @GetMapping
    fun getFileUploadForm() = "example/fileUpload"

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun postmappedvalue(
        @RequestParam("file") file: MultipartFile,
        model: Model,
    ): String {
        model.addAttribute(
            "fileUploadResponse",
            mapOf(
                "name" to file.originalFilename,
                "size" to file.size,
                "contentType" to file.contentType,
            ),
        )
        return "example/fileUpload"
    }
}
