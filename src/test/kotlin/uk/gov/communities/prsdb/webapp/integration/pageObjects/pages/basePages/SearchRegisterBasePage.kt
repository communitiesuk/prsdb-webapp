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

    fun getFilterPanel() = FilterPanel(page)

    fun getResultTable() = Table(page)

    fun getPaginationComponent() = Pagination(page)

    fun getErrorMessageText() = getErrorMessage().innerText()

    fun getErrorMessage(): Locator = page.locator("#no-results")
}
