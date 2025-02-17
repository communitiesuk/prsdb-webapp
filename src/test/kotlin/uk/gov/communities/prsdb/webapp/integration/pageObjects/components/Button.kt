package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Page.LocatorOptions

class Button(
    override val locator: Locator,
) : BaseComponent(locator),
    ClickAndWaitable {
    companion object {
        fun default(page: Page) = factory(page, null)

        fun byText(
            page: Page,
            text: String,
        ): Button = factory(page, text)

        private fun factory(
            page: Page,
            text: String?,
        ): Button =
            Button(
                page.locator(
                    ".govuk-button",
                    if (text == null) {
                        null
                    } else {
                        LocatorOptions().setHasText(text)
                    },
                ),
            )
    }
}
