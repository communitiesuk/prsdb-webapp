package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.ERROR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FILE_TOO_LARGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PUBLIC_PATH_SEGMENT

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

    @RequestMapping(LANDLORD_ERROR_ROUTE)
    fun handleLandlordError(
        request: HttpServletRequest,
        model: Model,
    ): String = handleError(request, model)

    @RequestMapping(LOCAL_AUTHORITY_ERROR_ROUTE)
    fun handleLocalAuthorityError(
        request: HttpServletRequest,
        model: Model,
    ): String = handleError(request, model)

    @RequestMapping(PUBLIC_ERROR_ROUTE)
    fun handlePublicError(
        request: HttpServletRequest,
        model: Model,
    ): String = handleError(request, model)

    @GetMapping("$ERROR_PATH_SEGMENT/$FILE_TOO_LARGE_PATH_SEGMENT")
    fun fileTooLargeErrorPage() = "error/fileTooLarge"

    @GetMapping("$LANDLORD_ERROR_ROUTE/$FILE_TOO_LARGE_PATH_SEGMENT")
    fun handleLandlordFileTooLargeErrorPage() = fileTooLargeErrorPage()

    @GetMapping("$LOCAL_AUTHORITY_ERROR_ROUTE/$FILE_TOO_LARGE_PATH_SEGMENT")
    fun handleLocalAuthorityFileTooLargeErrorPage() = fileTooLargeErrorPage()

    @GetMapping("$PUBLIC_ERROR_ROUTE/$FILE_TOO_LARGE_PATH_SEGMENT")
    fun handlePublicFileTooLargeErrorPage() = fileTooLargeErrorPage()

    companion object {
        const val LANDLORD_ERROR_ROUTE = "/$LANDLORD_PATH_SEGMENT/$ERROR_PATH_SEGMENT"
        const val PUBLIC_ERROR_ROUTE = "/$PUBLIC_PATH_SEGMENT/$ERROR_PATH_SEGMENT"
        const val LOCAL_AUTHORITY_ERROR_ROUTE = "/$LOCAL_AUTHORITY_PATH_SEGMENT/$ERROR_PATH_SEGMENT"
    }
}
