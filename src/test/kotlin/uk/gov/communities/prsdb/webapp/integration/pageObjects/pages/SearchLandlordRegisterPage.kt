package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getChildComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Pagination
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SearchBar
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class SearchLandlordRegisterPage(
    page: Page,
) : BasePage(page, "/search/landlord") {
    val searchBar = SearchBar(page)

    fun getResultTable() = Table(page)

    fun getLandlordLink(rowIndex: Int) = getChildComponent(getResultTable().getCell(rowIndex, LANDLORD_COL_INDEX), "a")

    fun getPaginationComponent() = Pagination(page)

    fun getErrorMessageText() = getErrorMessage().innerText()

    fun getPropertySearchLink() = getChildComponent(getErrorMessage(), "a")

    fun getErrorMessage() = getComponent(page, "#no-results")

    companion object {
        const val LANDLORD_COL_INDEX: Int = 0
        const val ADDRESS_COL_INDEX: Int = 1
        const val CONTACT_INFO_COL_INDEX: Int = 2
    }
}
