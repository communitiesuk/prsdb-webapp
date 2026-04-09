package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class SummaryList(
    parentLocator: Locator,
    index: Int? = null,
) : BaseComponent(if (index != null) parentLocator.locator(DEFAULT_SELECCTOR).nth(index) else parentLocator.locator(DEFAULT_SELECCTOR)) {
    constructor(page: Page, index: Int? = null) : this(page.locator("html"), index)

    protected fun getRow(key: String) = SummaryListRow.byKey(locator, key)

    protected fun getRow(index: Int) = SummaryListRow.byIndex(locator, index)

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

            fun byIndex(
                parentLocator: Locator,
                index: Int,
            ) = SummaryListRow(
                parentLocator.locator(".govuk-summary-list__row").nth(index),
            )
        }

        val key: Locator = locator.locator(".govuk-summary-list__key")
        val value: Locator = locator.locator(".govuk-summary-list__value")
        val actions = SummaryListRowActions(locator)

        fun valueLinkByText(text: String): Link = Link.byText(value, text)

        fun clickFirstActionLinkAndWait() = actions.firstActionLink.clickAndWait()

        fun clickNamedActionLinkAndWait(name: String) = actions.getActionLink(name).clickAndWait()
    }

    class SummaryListRowActions(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator.locator(".govuk-summary-list__actions")) {
        val firstActionLink = Link(locator.locator(".govuk-link").first())

        fun getActionLink(text: String) =
            SummaryListRowActionLink(
                locator.locator(
                    ".govuk-summary-list__actions-list-item",
                    Locator.LocatorOptions().setHasText(text),
                ),
            )

        fun getActionLink(index: Int) = SummaryListRowActionLink(locator.locator(".govuk-summary-list__actions-list-item").nth(index))

        fun getAllActionLinks(): List<SummaryListRowActionLink> {
            val count = locator.locator(".govuk-summary-list__actions-list-item").count()
            return (0 until count).map { getActionLink(it) }
        }
    }

    class SummaryListRowActionLink(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator) {
        val link = Link.default(locator)

        fun clickAndWait() = link.clickAndWait()
    }

    companion object {
        private const val DEFAULT_SELECCTOR = ".govuk-summary-list"
    }
}
