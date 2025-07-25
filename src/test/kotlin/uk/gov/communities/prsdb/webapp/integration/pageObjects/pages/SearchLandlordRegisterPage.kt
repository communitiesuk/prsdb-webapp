package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.SearchRegisterController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SearchRegisterBasePage

class SearchLandlordRegisterPage(
    page: Page,
) : SearchRegisterBasePage(page, SearchRegisterController.SEARCH_LANDLORD_URL) {
    fun getLandlordLink(rowIndex: Int) = resultTable.getClickableCell(rowIndex, LANDLORD_COL_INDEX).link

    fun getPropertySearchLink() = Link(noResultErrorMessage.locator("a"))

    val backLink = BackLink.default(page)

    companion object {
        const val LANDLORD_COL_INDEX: Int = 0
        const val ADDRESS_COL_INDEX: Int = 1
        const val CONTACT_INFO_COL_INDEX: Int = 2
        const val LISTED_PROPERTY_COL_INDEX: Int = 3
    }
}
