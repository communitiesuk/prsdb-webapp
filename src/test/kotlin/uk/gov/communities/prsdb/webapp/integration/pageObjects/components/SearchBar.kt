package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class SearchBar(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".moj-search")) {
    constructor(page: Page) : this(page.locator("html"))

    private val form = SearchBarForm(locator)

    fun search(searchTerm: String) {
        form.searchInput.fill(searchTerm)
        form.submit()
    }

    class SearchBarForm(
        parentLocator: Locator,
    ) : Form(parentLocator) {
        val searchInput: Locator = locator.locator("input#searchTerm")
    }
}
