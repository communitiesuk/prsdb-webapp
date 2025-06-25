package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.SearchRegisterController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SearchRegisterBasePage

class SearchPropertyRegisterPage(
    page: Page,
) : SearchRegisterBasePage(page, SearchRegisterController.SEARCH_PROPERTY_URL) {
    fun getPropertyLink(rowIndex: Int) = resultTable.getClickableCell(rowIndex, PROPERTY_COL_INDEX).link

    fun getLandlordLink(rowIndex: Int) = resultTable.getClickableCell(rowIndex, PROPERTY_LANDLORD_COL_INDEX).link

    fun getLandlordSearchLink() = Link(noResultErrorMessage.locator("a"))

    companion object {
        const val PROPERTY_COL_INDEX: Int = 0
        const val REG_NUM_COL_INDEX: Int = 1
        const val LA_COL_INDEX: Int = 2
        const val PROPERTY_LANDLORD_COL_INDEX: Int = 3
    }
}
