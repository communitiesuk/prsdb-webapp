package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class BackLink(
    override val locator: Locator,
) : BaseComponent(locator),
    ClickAndWaitable {
    companion object {
        fun default(parentLocator: Locator) = BackLink(parentLocator.locator(".govuk-back-link"))

        fun default(page: Page) = default(page.locator("html"))
    }
}
