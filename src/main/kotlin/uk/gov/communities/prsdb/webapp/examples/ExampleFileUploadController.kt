package uk.gov.communities.prsdb.webapp.examples

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.fileupload2.core.FileItemInputIterator
import org.apache.commons.io.FilenameUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.MapBindingResult
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import uk.gov.communities.prsdb.webapp.config.filters.MultipartFormDataFilter
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.helpers.MaximumLengthInputStream.Companion.withMaxLength
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.getFirstFileField
import uk.gov.communities.prsdb.webapp.services.CookieService
import uk.gov.communities.prsdb.webapp.services.FileUploader
import java.security.Principal

@Controller
// This free segment allows the example controller to simulate multiple journeys in parallel
@RequestMapping("example/$FILE_UPLOAD_URL_SUBSTRING/{freeSegment}")
class ExampleFileUploadController(
    private val fileUploader: FileUploader,
    private val cookieService: CookieService,
) {
    @GetMapping
    fun getFileUploadForm(
        response: HttpServletResponse,
        request: HttpServletRequest,
        @PathVariable("freeSegment") freeSegment: String,
        model: Model,
    ): String {
        addFlashValidationToModel(model)

        val cookie = cookieService.createCookieForValue(COOKIE_NAME, request.requestURI)
        response.addCookie(cookie)
        return "forms/uploadCertificateForm"
    }

    @PostMapping
    fun uploadFile(
        streamlessRequest: HttpServletRequest,
        @RequestAttribute(MultipartFormDataFilter.ITERATOR_ATTRIBUTE) iterator: FileItemInputIterator,
        @CookieValue(value = COOKIE_NAME) token: String,
        model: Model,
        @PathVariable("freeSegment") freeSegment: String,
        principal: Principal,
        redirectAttrs: RedirectAttributes,
    ): String {
        if (!cookieService.isTokenForCookieValue(token, streamlessRequest.requestURI)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload token")
        } else {
            cookieService.useToken(token)
        }

        // Currently we don't gracefully handle a request with multiple items - we take the first file and ignore the rest
        // If there's enough data in the subsequent requests this will cause the requests to not be read off the socket
        // and the browser will interpret that as a lost connection. This is only ok because there is no way for the
        // client to legitimately send multiple files to this endpoint - so we're happy with undefined behaviour as long
        // as it is safe - which this is for us.
        // To change this we just need to call next on the iterator for each item - which will read and discard the data.
        val file =
            iterator.getFirstFileField()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid multipart file upload request")

        // Because this is an example endpoint, we can just keep the file name uploaded - for the compliance journey
        // this will need to be a useful name for LA users to download (and we should not trust the uploaded file name)
        val key = "${principal.name}/$freeSegment/${file.name}"

        val errorMessageKeys = validateFileUploadRequest(file, streamlessRequest)
        if (errorMessageKeys.isNotEmpty()) {
            redirectAttrs.addFlashAttribute("errorMessageKeys", errorMessageKeys)

            // This swallows the rest of the file, which may cause a problem for very large files - e.g. 100s of TB
            iterator.hasNext()
            return "redirect:$freeSegment"
        }

        // This manually adds the formModel and binding result to the model - in this case that is all it does as
        // this code is only reached if validation passed
        addFlashValidationToModel(model)

        val uploadOutcome = fileUploader.uploadFile(key, file.inputStream.withMaxLength(streamlessRequest.contentLengthLong))
        model.addAttribute(
            "fileUploadResponse",
            mapOf(
                "uploadedName" to file.name,
                "uploadReturnValue" to uploadOutcome,
                "request contentType" to streamlessRequest.contentType,
                "cookie-value" to token,
            ),
        )
        return "forms/uploadCertificateForm"
    }

    private fun validateFileUploadRequest(
        file: FileItemInput,
        streamlessRequest: HttpServletRequest,
    ): List<String> {
        val errors = mutableListOf<String>()

        val exampleMaxFileSizeInBytes = 5L * 1024L * 1024L
        val exampleAllowedExtensions = listOf("pdf", "png", "mp4")
        val exampleMimetypes = listOf("application/pdf", "image/png", "video/mp4")

        if (exampleMaxFileSizeInBytes < streamlessRequest.contentLengthLong) {
            errors.add("file.error.tooBig")
        }
        if (!exampleAllowedExtensions.contains(FilenameUtils.getExtension(file.name))) {
            errors.add("file.error.badExtension")
        }
        if (!exampleMimetypes.contains(file.contentType)) {
            errors.add("file.error.badMimeType")
        }

        return errors
    }

    private fun addFlashValidationToModel(model: Model) {
        val bindingResult = MapBindingResult(mapOf("certificate" to null), "formModel")
        model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "formModel", bindingResult)
        val errors = model.getAttribute("errorMessageKeys") as? List<*>
        errors?.forEach {
            if (it is String) {
                bindingResult.rejectValue("certificate", "", it)
            }
        }
    }

    companion object {
        const val COOKIE_NAME = "example-file-upload-token"
    }
}
