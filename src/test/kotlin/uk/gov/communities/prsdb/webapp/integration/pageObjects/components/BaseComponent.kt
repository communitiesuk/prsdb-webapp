package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Page.LocatorOptions
import org.junit.jupiter.api.Assertions.assertEquals
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import kotlin.reflect.KClass

abstract class BaseComponent(
    protected val locator: Locator,
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

        fun getSubmitButton(page: Page) = getComponent(page, "button[type='submit']")
    }

    init {
        assertLocatorIsValid(locator)
    }

    protected fun getChildComponent(
        locatorStr: String,
        locatorOptions: Locator.LocatorOptions? = null,
        index: Int = 0,
    ): Locator = Companion.getChildComponent(locator, locatorStr, locatorOptions, index)

    protected fun <T : BasePage> clickChildElementAndAssertNextPage(
        locator: Locator,
        page: Page,
        nextPageClass: KClass<T>,
    ): T {
        locator.click()
        return createValidPage(page, nextPageClass)
    }
}
