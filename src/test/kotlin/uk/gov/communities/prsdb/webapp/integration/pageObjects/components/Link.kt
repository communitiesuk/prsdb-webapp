package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Locator.LocatorOptions
import com.microsoft.playwright.Page

class Link(
    override val locator: Locator,
) : BaseComponent(locator),
    ClickAndWaitable {
    companion object {
        fun byText(
            parentLocator: Locator,
            text: String,
            index: Int = 0,
        ): Link = Link(parentLocator.locator(".govuk-link", LocatorOptions().setHasText(text)).nth(index))

        fun byText(
            page: Page,
            text: String,
            index: Int = 0,
        ): Link = byText(page.locator("html"), text, index)
    }
}
