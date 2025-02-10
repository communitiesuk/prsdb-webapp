package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getComponent
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

    fun getHiddenResultTable() = Table.getLocator(page)

    fun getPaginationComponent() = Pagination(page)

    fun getHiddenPaginationComponent() = Pagination.getLocator(page)

    fun getErrorMessageText() = getErrorMessage().innerText()

    fun getErrorMessage(isVisible: Boolean = true) = if (isVisible) getComponent(page, "#no-results") else page.locator("#no-results")
}
