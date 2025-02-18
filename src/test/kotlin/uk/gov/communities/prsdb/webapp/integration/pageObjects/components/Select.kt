package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Select(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".autocomplete__wrapper")) {
    constructor(page: Page) : this(page.locator("html"))

    val autocompleteInput = locator.locator("input")

    fun selectValue(value: String) = locator.locator("li", Locator.LocatorOptions().setHasText(value)).click()
}
