package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Heading(
    locator: Locator,
) : BaseComponent(locator) {
    fun getText(): String = locator.textContent()

    companion object {
        fun default(page: Page) = default(page.locator("html"))

        fun default(parentLocator: Locator) = Heading(parentLocator.locator("main header h1"))
    }
}
