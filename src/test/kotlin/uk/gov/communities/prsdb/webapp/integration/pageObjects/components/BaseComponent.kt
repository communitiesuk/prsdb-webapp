package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Page.LocatorOptions
import com.microsoft.playwright.assertions.LocatorAssertions
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat

abstract class BaseComponent(
    protected open val locator: Locator,
) {
    companion object {
        fun assertThat(component: BaseComponent): LocatorAssertions = assertThat(component.locator)

        // TODO PRSD-884 Delete
        fun getComponent(
            page: Page,
            locatorStr: String,
            locatorOptions: LocatorOptions? = null,
            index: Int = 0,
        ): Locator {
            val component = page.locator(locatorStr, locatorOptions).nth(index)
            return component
        }

        // TODO PRSD-884 Delete
        fun getChildComponent(
            parentLocator: Locator,
            locatorStr: String,
            locatorOptions: Locator.LocatorOptions? = null,
            index: Int = 0,
        ): Locator {
            val component = parentLocator.locator(locatorStr, locatorOptions).nth(index)
            return component
        }

        // TODO PRSD-884 Delete
        fun getChildrenComponents(
            parentLocator: Locator,
            locatorStr: String,
            locatorOptions: Locator.LocatorOptions? = null,
        ): List<Locator> {
            val component = parentLocator.locator(locatorStr, locatorOptions)
            return component.all()
        }

        fun getConfirmationPageBanner(page: Page) = getComponent(page, ".govuk-panel--confirmation")
    }

    protected fun getChildrenComponents(
        locatorStr: String,
        locatorOptions: Locator.LocatorOptions? = null,
    ): List<Locator> = Companion.getChildrenComponents(locator, locatorStr, locatorOptions)

    protected fun getChildComponent(
        locatorStr: String,
        locatorOptions: Locator.LocatorOptions? = null,
        index: Int = 0,
    ): Locator = Companion.getChildComponent(locator, locatorStr, locatorOptions, index)
}
