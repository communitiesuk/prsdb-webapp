package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.COOKIES_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.LA_COOKIES_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.LL_COOKIES_ROUTE

@PrsdbController
@RequestMapping(COOKIES_ROUTE, LL_COOKIES_ROUTE, LA_COOKIES_ROUTE)
class CookiesController {
    @GetMapping
    fun getCookiesPage(): String = "cookies"

    companion object {
        const val COOKIES_ROUTE = "/$COOKIES_PATH_SEGMENT"
        const val LL_COOKIES_ROUTE = "/$LANDLORD_PATH_SEGMENT$COOKIES_ROUTE"
        const val LA_COOKIES_ROUTE = "/$LOCAL_AUTHORITY_PATH_SEGMENT$COOKIES_ROUTE"
    }
}
