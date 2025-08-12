package uk.gov.communities.prsdb.webapp.controllers.controllerAdvice

import jakarta.servlet.http.HttpServletRequest
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import uk.gov.communities.prsdb.webapp.PrsdbControllerAdvice
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
                path.startsWith("/local-authority") -> "/local-authority/beta-banner"
                path.startsWith("/landlord") -> "/landlord/beta-banner"
                else -> "/landlord/beta-banner"
            }
        model.addAttribute("feedbackBannerUrl", url)
    }
}
