package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Page.LocatorOptions

class Button(
    locator: Locator,
) : BaseComponent(locator) {
    companion object {
        fun byText(
            page: Page,
            text: String? = null,
        ): Button = Button(page.locator(".govuk-button", if (text == null) null else LocatorOptions().setHasText(text)))
    }

    fun clickAndWait() {
        locator.click()
        locator.page().waitForLoadState()
    }
}
