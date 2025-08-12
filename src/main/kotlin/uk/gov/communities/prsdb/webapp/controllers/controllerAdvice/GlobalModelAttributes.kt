package uk.gov.communities.prsdb.webapp.controllers.controllerAdvice

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import uk.gov.communities.prsdb.webapp.PrsdbControllerAdvice
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_USER
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE

@PrsdbControllerAdvice
class GlobalModelAttributes {
    @ModelAttribute
    fun addGlobalModelAttributes(model: Model) {
        model.addAttribute("cookiesUrl", COOKIES_ROUTE)
    }

    @ModelAttribute("feedbackUrl")
    fun addFeedbackUrl(
        model: Model,
        auth: org.springframework.security.core.Authentication?,
    ) {
        val url =
            if (auth != null && auth.isAuthenticated &&
                auth.authorities.any { it.authority == ROLE_LA_USER || it.authority == ROLE_LA_ADMIN }
            ) {
                "/local-authority/feedback"
            } else {
                "/landlord/feedback"
            }
        model.addAttribute("feedbackUrl", url)
    }
}
