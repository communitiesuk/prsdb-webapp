package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.COOKIES_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DASHBOARD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@PrsdbController
@RequestMapping(COOKIES_ROUTE)
class CookiesController {
    @GetMapping
    fun getCookiesPage(model: Model): String {
        model.addAttribute(BACK_URL_ATTR_NAME, DASHBOARD_PATH_SEGMENT)

        // We use an anonymous form model object as JavaScript is used to handle form submission
        model.addAttribute(
            "consentWrapper",
            object {
                var consent: Boolean? = null
            },
        )
        model.addAttribute(
            "radioOptions",
            listOf(
                RadiosButtonViewModel(value = true, valueStr = "yes", labelMsgKey = "forms.radios.option.yes.label"),
                RadiosButtonViewModel(value = false, valueStr = "no", labelMsgKey = "forms.radios.option.no.label"),
            ),
        )

        return "cookies"
    }

    companion object {
        const val COOKIES_ROUTE = "/$COOKIES_PATH_SEGMENT"
    }
}
