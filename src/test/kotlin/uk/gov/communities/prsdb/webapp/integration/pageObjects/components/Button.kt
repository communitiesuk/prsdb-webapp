package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class Button(
    override val locator: Locator,
) : BaseComponent(locator),
    ClickAndWaitable {
    companion object {
        fun default(parentLocator: Locator) = factory(parentLocator)

        fun default(page: Page) = factory(page)

        fun byText(
            parentLocator: Locator,
            text: String? = null,
        ): Button = factory(parentLocator, text)

        fun byText(
            page: Page,
            text: String,
        ): Button = factory(page, text)

        private fun factory(
            page: Page,
            text: String? = null,
        ): Button = factory(page.locator("html"), text)

        private fun factory(
            parentLocator: Locator,
            text: String? = null,
        ): Button =
            Button(
                parentLocator.locator(
                    ".govuk-button",
                    Locator.LocatorOptions().setHasText(text),
                ),
            )
    }
}
