package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

abstract class ErrorPage(
    page: Page,
    urlSegment: String? = null,
) : BasePage(page, urlSegment) {
    val heading = Heading(page.locator("main h1"))
    val description: Locator = page.locator("main p.govuk-body")
}
