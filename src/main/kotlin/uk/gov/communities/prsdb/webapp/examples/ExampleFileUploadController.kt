package uk.gov.communities.prsdb.webapp.examples

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.fileupload2.core.FileItemInputIterator
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.config.filters.MultipartFormDataFilter
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.examples.MaximumLengthInputStream.Companion.withMaxLength
import java.security.Principal

@Controller
// This free segment allows the example controller to simulate multiple journeys in parallel
@RequestMapping("example/$FILE_UPLOAD_URL_SUBSTRING/{freeSegment}")
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
        @RequestAttribute(MultipartFormDataFilter.ITERATOR_ATTRIBUTE) iterator: FileItemInputIterator,
        @CookieValue(value = COOKIE_NAME) token: String,
        model: Model,
        @PathVariable("freeSegment") freeSegment: String,
        principal: Principal,
    ): String {
        if (!tokenService.checkTokenIsFor(token, freeSegment)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload token")
        }

        val file =
            getFirstFileItem(iterator) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid multipart file upload request")

        // Because this is an example endpoint, we can just keep the file name uploaded - for the compliance journey
        // this will need to be a useful name for LA users to download (and we should not trust the uploaded file name)
        val key = "${principal.name}/$freeSegment/${file.name}"

        val exampleMaxFileSizeInBytes = 5L * 1024L * 1024L

        val uploadOutcome = fileUploader.uploadFile(key, file.inputStream.withMaxLength(exampleMaxFileSizeInBytes))
        model.addAttribute(
            "fileUploadResponse",
            mapOf(
                "uploadedName" to file.name,
                "uploadReturnValue" to uploadOutcome,
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
        tokenCookie.path = "/example/$FILE_UPLOAD_URL_SUBSTRING/$segment"
        tokenCookie.secure = true

        return tokenCookie
    }

    private fun getFirstFileItem(singleFileIterator: FileItemInputIterator): FileItemInput? {
        if (!singleFileIterator.hasNext()) {
            return null
        }

        // Currently we don't gracefully handle a request with multiple items - we take the first file and ignore the rest
        // If there's enough data in the subsequent requests this will cause the requests to not be read off the socket
        // and the browser will interpret that as a lost connection. This is only ok because there is no way for the
        // client to legitimately send multiple files to this endpoint - so we're happy with undefined behaviour as long
        // as it is safe - which this is for us.
        // To change this we just need to call next on the iterator for each item - which will read and discard the data.
        var firstItem = singleFileIterator.next()

        while (firstItem.isFormField && singleFileIterator.hasNext()) {
            firstItem = singleFileIterator.next()
        }

        if (firstItem.isFormField) {
            return null
        }

        return firstItem
    }

    companion object {
        const val COOKIE_NAME = "example-file-upload-token"
    }
}
