package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class InsetText(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".govuk-inset-text")) {
    constructor(page: Page) : this(page.locator("html"))

    val spanText = getChildComponent("span")
}
