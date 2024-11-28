package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Page.LocatorOptions
import org.junit.jupiter.api.Assertions.assertEquals

abstract class BaseComponent(
    private val locator: Locator,
) {
    companion object {
        private fun assertLocatorIsValid(locator: Locator) {
            assertEquals(1, locator.count(), "Expected 1 instance of $locator, found ${locator.count()}")
        }

        private fun getComponent(
            page: Page,
            locatorStr: String,
            locatorOptions: LocatorOptions? = null,
            index: Int = 0,
        ): Locator {
            val component = page.locator(locatorStr, locatorOptions).nth(index)
            assertLocatorIsValid(component)
            return component
        }

        fun getChildComponent(
            parentLocator: Locator,
            locatorStr: String,
            locatorOptions: Locator.LocatorOptions? = null,
            index: Int = 0,
        ): Locator {
            val component = parentLocator.locator(locatorStr, locatorOptions).nth(index)
            assertLocatorIsValid(component)
            return component
        }

        fun getSection(
            page: Page,
            index: Int = 0,
        ) = getComponent(page, "section", index = index)

        fun getHeading(page: Page) = getComponent(page, "main header h1")

        fun getSubHeading(page: Page) = getComponent(page, "main header p")

        fun getConfirmationPageBanner(page: Page) = getComponent(page, ".govuk-panel--confirmation")

        fun getButton(
            page: Page,
            text: String? = null,
        ) = getComponent(page, ".govuk-button", if (text == null) null else LocatorOptions().setHasText(text))
    }

    init {
        assertLocatorIsValid(locator)
    }

    protected fun getChildComponent(
        locatorStr: String,
        locatorOptions: Locator.LocatorOptions? = null,
        index: Int = 0,
    ): Locator = Companion.getChildComponent(locator, locatorStr, locatorOptions, index)
}
