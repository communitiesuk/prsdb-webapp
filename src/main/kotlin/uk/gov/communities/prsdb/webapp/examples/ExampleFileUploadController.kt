package uk.gov.communities.prsdb.webapp.examples

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.security.Principal

@Controller
// This free segment allows the example controller to simulate multiple journeys in parallel
@RequestMapping("example/file-upload/{freeSegment}")
class ExampleFileUploadController(
    private val fileUploader: FileUploader,
    private val tokenService: FileUploadTokenService,
) {
    @GetMapping
    fun getFileUploadForm(
        response: HttpServletResponse,
        @PathVariable("freeSegment") freeSegment: String,
    ): String {
        val token = tokenService.issueTokenFor(freeSegment)
        val tokenCookie = createFileUploadTokenCookie(token, freeSegment)
        response.addCookie(tokenCookie)
        return "example/fileUpload"
    }

    @PostMapping
    fun uploadFile(
        @RequestParam("uploaded-file") file: MultipartFile,
        @CookieValue(value = COOKIE_NAME) token: String,
        model: Model,
        @PathVariable("freeSegment") freeSegment: String,
        principal: Principal,
    ): String {
        if (!tokenService.checkTokenIsFor(token, freeSegment)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload token")
        }

        val key = "${principal.name}/$freeSegment/${file.originalFilename}"

        val uploadOutcome = fileUploader.uploadFile(key, file.inputStream, file.size)
        model.addAttribute(
            "fileUploadResponse",
            mapOf(
                "uploadedName" to file.originalFilename,
                "uploadReturnValue" to uploadOutcome,
                "size" to file.size,
                "contentType" to file.contentType,
                "cookie-value" to token,
            ),
        )
        return "example/fileUpload"
    }

    private fun createFileUploadTokenCookie(
        token: String,
        segment: String,
    ): Cookie {
        // This cookie needs to be used in a way such that parallel journeys in the same session can be supported
        // for example it could use the path attribute can be used to differentiate between them alongside validating
        // that the token is being used correctly by storing some additional information with the token in the session.
        val tokenCookie = Cookie(COOKIE_NAME, token)
        tokenCookie.isHttpOnly = true
        tokenCookie.path = "/example/file-upload/$segment"
        tokenCookie.secure = true

        return tokenCookie
    }

    companion object {
        const val COOKIE_NAME = "example-file-upload-token"
    }
}
