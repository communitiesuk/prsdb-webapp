package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class SearchBar(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".moj-search")) {
    constructor(page: Page) : this(page.locator("html"))

    private val form = Form(locator)
    private val searchInput = form.getTextInput()

    fun search(searchTerm: String) {
        searchInput.fill(searchTerm)
        form.submit()
    }
}
