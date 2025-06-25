package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.ERROR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FILE_TOO_LARGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordCustomErrorController.Companion.LANDLORD_ERROR_ROUTE

@PrsdbController
@RequestMapping(LANDLORD_ERROR_ROUTE)
class LandlordCustomErrorController : CustomErrorController {
    @GetMapping("/$FILE_TOO_LARGE_PATH_SEGMENT")
    fun fileTooLargeErrorPage() = commonFileTooLargeErrorPage()

    @RequestMapping
    fun handleError(
        request: HttpServletRequest,
        model: Model,
    ): String = commonHandleError(request, model)

    companion object {
        const val LANDLORD_ERROR_ROUTE = "/$LANDLORD_PATH_SEGMENT/$ERROR_PATH_SEGMENT"
    }
}
