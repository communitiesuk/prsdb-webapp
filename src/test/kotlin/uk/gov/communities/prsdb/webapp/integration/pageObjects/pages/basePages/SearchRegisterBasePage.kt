package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FilterPanel
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Pagination
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SearchBar
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table

abstract class SearchRegisterBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val searchBar = SearchBar(page)

    val filterPanel = FilterPanel(page)

    val resultTable = Table(page)

    val paginationComponent = Pagination(page)

    val errorMessageText: String?
        get() = noResultErrorMessage.innerText()

    val noResultErrorMessage: Locator = page.locator("#no-results")
}
