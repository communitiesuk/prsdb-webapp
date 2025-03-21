package uk.gov.communities.prsdb.webapp.examples

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping("example/file-upload")
class ExampleFileUploadController(
    val fileUploader: FileUploader,
) {
    @GetMapping
    fun getFileUploadForm() = "example/fileUpload"

    @PostMapping
    fun uploadFile(
        @RequestParam("uploaded-file") file: MultipartFile,
        model: Model,
    ): String {
        val extension = file.originalFilename?.let { it.substring(it.lastIndexOf('.') + 1) }
        val uploadOutcome = fileUploader.uploadFile(file.inputStream, extension)
        model.addAttribute(
            "fileUploadResponse",
            mapOf(
                "uploadedName" to file.originalFilename,
                "uploadReturnValue" to uploadOutcome,
                "size" to file.size,
                "contentType" to file.contentType,
            ),
        )
        return "example/fileUpload"
    }
}
