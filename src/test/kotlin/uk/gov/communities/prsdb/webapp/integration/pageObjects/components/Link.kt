package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Page.LocatorOptions

class Link(
    override val locator: Locator,
) : BaseComponent(locator),
    ClickAndWaitable {
    companion object {
        fun byText(
            page: Page,
            text: String,
            index: Int = 0,
        ): Link = Link(page.locator(".govuk-link", LocatorOptions().setHasText(text)).nth(index))
    }
}
