package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.ERROR_PATH_SEGMENT

@PrsdbController
class CustomErrorController : ErrorController {
    @RequestMapping(ERROR_PATH_SEGMENT)
    fun handleError(
        request: HttpServletRequest,
        model: Model,
    ): String {
        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)
        val statusCode = status?.toString()?.toInt()
        return when (statusCode) {
            HttpStatus.NOT_FOUND.value() -> "error/404"
            HttpStatus.FORBIDDEN.value() -> "error/403"
            else -> "error/500"
        }
    }
}
