package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Select(
    private val page: Page,
    locator: Locator = page.locator(".autocomplete__wrapper"),
) : BaseComponent(locator) {
    val autocompleteInput = getChildComponent("input")

    fun selectValue(value: String) = getChildComponent("li", Locator.LocatorOptions().setHasText(value)).click()
}
