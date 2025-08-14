package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.NotificationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CookiesPage(
    page: Page,
) : BasePage(page, COOKIES_ROUTE) {
    val backLink = BackLink.default(page)
    val successBanner = SuccessBanner(page)
    val heading = Heading(page.locator("main h1"))
    val consentForm = ConsentForm(page)

    class SuccessBanner(
        page: Page,
    ) : NotificationBanner(page) {
        val backLink = Link(page.locator(".govuk-notification-banner__link"))
    }

    class ConsentForm(
        page: Page,
    ) : Form(page) {
        val radios = Radios(page)
    }
}
