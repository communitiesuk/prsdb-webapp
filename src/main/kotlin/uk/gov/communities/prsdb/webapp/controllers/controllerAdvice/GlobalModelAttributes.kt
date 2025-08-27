package uk.gov.communities.prsdb.webapp.controllers.controllerAdvice

import org.springframework.beans.factory.annotation.Value
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import uk.gov.communities.prsdb.webapp.annotations.PrsdbControllerAdvice
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.constants.CROWN_COPYRIGHT_URL
import uk.gov.communities.prsdb.webapp.constants.GOV_LICENCE_URL
import uk.gov.communities.prsdb.webapp.constants.MHCLG_URL
import uk.gov.communities.prsdb.webapp.constants.PRIVACY_NOTICE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PRSD_EMAIL
import uk.gov.communities.prsdb.webapp.constants.RENTERS_RIGHTS_BILL_URL
import uk.gov.communities.prsdb.webapp.controllers.BetaFeedbackController.Companion.FEEDBACK_URL
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
        // Cookie banner attributes
        model.addAttribute("cookiesUrl", COOKIES_ROUTE.overrideBackLinkForUrl(backUrlStorageService.storeCurrentUrlReturningKey()))
        model.addAttribute("googleAnalyticsMeasurementId", gaMeasurementId)
        model.addAttribute("googleAnalyticsCookieDomain", gaCookieDomain)
        model.addAttribute("plausibleDomainId", plausibleDomainId)

        // Feedback banner attributes
        model.addAttribute("feedbackBannerUrl", FEEDBACK_URL)

        // Footer attributes
        model.addAttribute("prsdbEmail", PRSD_EMAIL)
        model.addAttribute("privacyUrl", "/$PRIVACY_NOTICE_PATH_SEGMENT")
        model.addAttribute("rentersRightsBillUrl", RENTERS_RIGHTS_BILL_URL)
        model.addAttribute("mhclgUrl", MHCLG_URL)
        model.addAttribute("licenceUrl", GOV_LICENCE_URL)
        model.addAttribute("copyrightUrl", CROWN_COPYRIGHT_URL)
    }
}
