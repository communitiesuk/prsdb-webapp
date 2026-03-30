package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService

@PrsdbWebService
class FileUploadCookieService(
    private val tokenCookieService: TokenCookieService,
    private val request: HttpServletRequest,
    private val response: HttpServletResponse,
) {
    fun addFileUploadCookieToResponse() {
        val cookie = tokenCookieService.createCookieForValue(FILE_UPLOAD_COOKIE_NAME, request.requestURI)
        response.addCookie(cookie)
    }

    fun validateAndUseToken(token: String) {
        if (tokenCookieService.isTokenForCookieValue(token, request.requestURI)) {
            tokenCookieService.useToken(token)
        } else {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload token")
        }
    }

    companion object {
        const val FILE_UPLOAD_COOKIE_NAME = "file-upload-cookie"
    }
}
