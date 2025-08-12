package uk.gov.communities.prsdb.webapp.controllers.controllerAdvice

import org.springframework.beans.factory.annotation.Value
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import uk.gov.communities.prsdb.webapp.PrsdbControllerAdvice
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE

@PrsdbControllerAdvice
class GlobalModelAttributes {
    @Value("\${google-analytics.measurement-id}")
    private lateinit var gaMeasurementId: String

    @ModelAttribute
    fun addGlobalModelAttributes(model: Model) {
        model.addAttribute("cookiesUrl", COOKIES_ROUTE)
        model.addAttribute("googleAnalyticsMeasurementId", gaMeasurementId)
    }
}
