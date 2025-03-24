package uk.gov.communities.prsdb.webapp.examples

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@Controller
@RequestMapping("example/file-upload")
class ExampleFileUploadController(
    private val fileUploader: FileUploader,
    private val tokenService: FileUploadTokenService,
) {
    @GetMapping
    fun getFileUploadForm(
        response: HttpServletResponse,
        model: Model,
    ): String {
        val token = tokenService.issueToken()
        val tokenCookie = createFileUploadTokenCookie(token)
        response.addCookie(tokenCookie)
        return "example/fileUpload"
    }

    @PostMapping
    fun uploadFile(
        @RequestParam("uploaded-file") file: MultipartFile,
        @CookieValue(value = "token-cookie") token: String,
        model: Model,
    ): String {
        if (!tokenService.checkToken(token)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload token")
        }

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

    private fun createFileUploadTokenCookie(token: String): Cookie {
        // This cookie name needs to uniquely identify the context in which this upload should occur, e.g. the exact
        // property and compliance file being uploaded, so that parallel journeys in the same session can be supported
        // OR it can have a generic name, and the path attribute can be used to differentiate between them.
        val tokenCookie = Cookie("example-file-upload-token", token)
        tokenCookie.isHttpOnly = true
        tokenCookie.path = "/example/file-upload"
        tokenCookie.secure = true

        // In non-example code, this max age property should be set thoughtfully - and potentially be configurable
        tokenCookie.maxAge = 60 * 60 * 2
        return tokenCookie
    }
}
