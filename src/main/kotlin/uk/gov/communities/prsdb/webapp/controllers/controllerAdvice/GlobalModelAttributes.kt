package uk.gov.communities.prsdb.webapp.controllers.controllerAdvice

import jakarta.servlet.http.HttpServletRequest
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import uk.gov.communities.prsdb.webapp.PrsdbControllerAdvice
import uk.gov.communities.prsdb.webapp.constants.BETA_BANNER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE

@PrsdbControllerAdvice
class GlobalModelAttributes {
    @ModelAttribute
    fun addGlobalModelAttributes(model: Model) {
        model.addAttribute("cookiesUrl", COOKIES_ROUTE)
    }

    @ModelAttribute("feedbackBannerUrl")
    fun addFeedbackBannerUrl(
        model: Model,
        request: HttpServletRequest,
    ) {
        val path = request.requestURI
        val url =
            when {
                path.startsWith("/$LOCAL_AUTHORITY_PATH_SEGMENT") -> "/$LOCAL_AUTHORITY_PATH_SEGMENT/$BETA_BANNER_PATH_SEGMENT"
                path.startsWith("/$LANDLORD_PATH_SEGMENT") -> "/$LANDLORD_PATH_SEGMENT/$BETA_BANNER_PATH_SEGMENT"
                else -> "/$LANDLORD_PATH_SEGMENT/$BETA_BANNER_PATH_SEGMENT"
            }
        model.addAttribute("feedbackBannerUrl", url)
    }
}
