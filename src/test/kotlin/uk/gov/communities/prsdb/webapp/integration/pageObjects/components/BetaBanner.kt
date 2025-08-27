package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class BetaBanner(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(SELECTOR)) {
    constructor(page: Page) : this(page.locator("html"))

    val giveFeedbackLink = Link.byText(parentLocator, "give your feedback")

    companion object {
        const val SELECTOR = ".govuk-phase-banner"
    }
}
