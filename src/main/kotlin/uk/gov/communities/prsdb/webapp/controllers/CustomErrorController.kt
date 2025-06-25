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
    @GetMapping("$LANDLORD_ERROR_ROUTE/$FILE_TOO_LARGE_PATH_SEGMENT")
    fun fileTooLargeErrorPageLandlordView() = commonFileTooLargeErrorPage()

    @RequestMapping(LANDLORD_ERROR_ROUTE)
    fun handleErrorLandlordView(
        request: HttpServletRequest,
        model: Model,
    ): String = commonHandleError(request, model)

    @GetMapping("$LOCAL_AUTHORITY_ERROR_ROUTE/$FILE_TOO_LARGE_PATH_SEGMENT")
    fun fileTooLargeErrorPageLAView() = commonFileTooLargeErrorPage()

    @RequestMapping(LOCAL_AUTHORITY_ERROR_ROUTE)
    fun handleErrorLAView(
        request: HttpServletRequest,
        model: Model,
    ): String = commonHandleError(request, model)

    @GetMapping("$PUBLIC_ERROR_ROUTE/$FILE_TOO_LARGE_PATH_SEGMENT")
    fun fileTooLargeErrorPagePublicView() = commonFileTooLargeErrorPage()

    @RequestMapping(PUBLIC_ERROR_ROUTE)
    fun handleErrorPublicView(
        request: HttpServletRequest,
        model: Model,
    ): String = commonHandleError(request, model)

    private fun commonHandleError(
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

    private fun commonFileTooLargeErrorPage() = "error/fileTooLarge"

    companion object {
        const val LANDLORD_ERROR_ROUTE = "/$LANDLORD_PATH_SEGMENT/$ERROR_PATH_SEGMENT"

        const val LOCAL_AUTHORITY_ERROR_ROUTE = "/$LOCAL_AUTHORITY_PATH_SEGMENT/$ERROR_PATH_SEGMENT"

        const val PUBLIC_ERROR_ROUTE = "/$PUBLIC_PATH_SEGMENT/$ERROR_PATH_SEGMENT"
    }
}
