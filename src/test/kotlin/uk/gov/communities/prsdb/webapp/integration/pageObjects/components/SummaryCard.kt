package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class SummaryCard(
    parentLocator: Locator,
    index: Int = 0,
) : BaseComponent(parentLocator.locator(".govuk-summary-card").nth(index)) {
    constructor(page: Page, index: Int = 0) : this(page.locator("html"), index)

    val title = Heading(locator.locator("h2.govuk-summary-card__title"))

    fun actions(text: String) = SummaryCardActions(locator, text)

    open val summaryCardList = SummaryList(locator)

    class SummaryCardActions(
        parentLocator: Locator,
        text: String,
    ) : BaseComponent(parentLocator.locator(".govuk-summary-card__actions")) {
        val actionLink = Link.byText(locator, text)
    }
}
