package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Warning(
    locator: Locator,
) : BaseComponent(locator) {
    companion object {
        fun default(page: Page) = default(page.locator("html"))

        fun default(parentLocator: Locator) = Warning(parentLocator.locator(".govuk-warning-text"))
    }
}
