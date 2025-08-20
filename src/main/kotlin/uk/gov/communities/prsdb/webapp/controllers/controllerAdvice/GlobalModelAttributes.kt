package uk.gov.communities.prsdb.webapp.controllers.controllerAdvice

import org.springframework.beans.factory.annotation.Value
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import uk.gov.communities.prsdb.webapp.PrsdbControllerAdvice
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService

@PrsdbControllerAdvice
class GlobalModelAttributes(
    private val backUrlStorageService: BackUrlStorageService,
) {
    @Value("\${google-analytics.measurement-id}")
    private lateinit var gaMeasurementId: String

    @Value("\${google-analytics.cookie-domain}")
    private lateinit var gaCookieDomain: String

    @Value("\${plausible.domain-id}")
    private lateinit var plausibleDomainId: String

    @ModelAttribute
    fun addGlobalModelAttributes(model: Model) {
        model.addAttribute("cookiesUrl", COOKIES_ROUTE.overrideBackLinkForUrl(backUrlStorageService.storeCurrentUrlReturningKey()))
        model.addAttribute("googleAnalyticsMeasurementId", gaMeasurementId)
        model.addAttribute("googleAnalyticsCookieDomain", gaCookieDomain)
        model.addAttribute("plausibleDomainId", plausibleDomainId)
    }

    @ModelAttribute("feedbackBannerUrl")
    fun addFeedbackBannerUrl(): String = "/feedback"
}
