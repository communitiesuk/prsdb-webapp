package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class PageNotFoundPage(
    page: Page,
) : BasePage(page) {
    override fun validate() {
        assertThat(heading).containsText("Page not found")
    }

    val heading = page.locator(".govuk-heading-l")
}
