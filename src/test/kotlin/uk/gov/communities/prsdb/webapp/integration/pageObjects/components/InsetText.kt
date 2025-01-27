package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class InsetText(
    private val page: Page,
    locator: Locator = page.locator(".govuk-inset-text"),
) : BaseComponent(locator) {
    val spanText = getChildComponent("span")
}
