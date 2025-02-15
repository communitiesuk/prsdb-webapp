package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class SearchBar(
    private val page: Page,
    locator: Locator = page.locator(".moj-search"),
) : BaseComponent(locator) {
    private val form = Form(page, parentLocator = locator)
    private val searchInput = form.getTextInput()

    fun search(searchTerm: String) {
        searchInput.fill(searchTerm)
        form.submit()
    }
}
