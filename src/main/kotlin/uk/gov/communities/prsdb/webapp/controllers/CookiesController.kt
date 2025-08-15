package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.COOKIES_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DASHBOARD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE

@PrsdbController
@RequestMapping(COOKIES_ROUTE)
class CookiesController {
    @GetMapping
    fun getCookiesPage(model: Model): String {
        model.addAttribute(BACK_URL_ATTR_NAME, DASHBOARD_PATH_SEGMENT)
        return "cookies"
    }

    companion object {
        const val COOKIES_ROUTE = "/$COOKIES_PATH_SEGMENT"
    }
}
