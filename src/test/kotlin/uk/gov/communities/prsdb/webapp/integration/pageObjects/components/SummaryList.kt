package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class SummaryList(
    parentLocator: Locator,
    index: Int? = null,
) : BaseComponent(if (index != null) parentLocator.locator(DEFAULT_SELECCTOR).nth(index) else parentLocator.locator(DEFAULT_SELECCTOR)) {
    constructor(page: Page, index: Int? = null) : this(page.locator("html"), index)

    protected fun getRow(key: String) = SummaryListRow.byKey(locator, key)

    class SummaryListRow(
        locator: Locator,
    ) : BaseComponent(locator) {
        companion object {
            fun byKey(
                parentLocator: Locator,
                key: String,
            ) = SummaryListRow(
                // Locate the row which has a key which has the given text
                parentLocator.locator(
                    ".govuk-summary-list__row",
                    Locator.LocatorOptions().setHas(
                        parentLocator.page().locator(
                            ".govuk-summary-list__key",
                            Page.LocatorOptions().setHasText(key),
                        ),
                    ),
                ),
            )
        }

        val key: Locator = locator.locator(".govuk-summary-list__key")
        val value: Locator = locator.locator(".govuk-summary-list__value")
        val actions = SummaryListRowActions(locator)

        fun clickActionLinkAndWait() = actions.actionLink.clickAndWait()
    }

    class SummaryListRowActions(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator.locator(".govuk-summary-list__actions")) {
        val actionLink = Link.default(locator)
    }

    companion object {
        private const val DEFAULT_SELECCTOR = ".govuk-summary-list"
    }
}
