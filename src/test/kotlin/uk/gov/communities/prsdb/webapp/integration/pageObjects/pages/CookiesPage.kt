package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CookiesPage(
    page: Page,
) : BasePage(page, COOKIES_ROUTE) {
    val heading = Heading(page.locator("main h1"))
}
