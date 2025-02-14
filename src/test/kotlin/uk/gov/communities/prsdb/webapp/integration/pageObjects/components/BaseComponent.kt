package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Page.LocatorOptions

abstract class BaseComponent(
    protected val locator: Locator,
) {
    companion object {
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

        fun getLink(
            page: Page,
            text: String,
            index: Int = 0,
        ) = getComponent(page, ".govuk-link", LocatorOptions().setHasText(text), index)
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
