package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class SummaryCard(
    locator: Locator,
) : BaseComponent(locator) {
    constructor(page: Page, index: Int = 0) : this(page.locator(DEFAULT_SELECTOR).nth(index))

    constructor(page: Page, title: String) : this(page.locator(DEFAULT_SELECTOR, Page.LocatorOptions().setHasText(title)))

    val title = Heading(locator.locator("h2.govuk-summary-card__title"))

    fun getAction(text: String) = SummaryCardAction(locator, text)

    open val summaryList = SummaryList(locator)

    class SummaryCardAction(
        parentLocator: Locator,
        text: String,
    ) : BaseComponent(parentLocator.locator(".govuk-summary-card__action", Locator.LocatorOptions().setHasText(text))) {
        val link = Link.default(locator)
    }

    companion object {
        private const val DEFAULT_SELECTOR = ".govuk-summary-card"
    }
}
