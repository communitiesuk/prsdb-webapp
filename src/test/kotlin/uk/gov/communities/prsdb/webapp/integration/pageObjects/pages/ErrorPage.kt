package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ErrorPage(
    page: Page,
) : BasePage(page) {
    val heading = Heading(page.locator("main h1"))
    val description: Locator = page.locator("main p.govuk-body")
}
