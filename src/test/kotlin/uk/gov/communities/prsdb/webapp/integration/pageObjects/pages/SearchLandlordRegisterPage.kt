package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SearchRegisterBasePage

class SearchLandlordRegisterPage(
    page: Page,
) : SearchRegisterBasePage(page, "/search/landlord") {
    fun getLandlordLink(rowIndex: Int) = Link(getResultTable().getCell(rowIndex, LANDLORD_COL_INDEX).locator("a"))

    fun getPropertySearchLink() = Link(getErrorMessage().locator("a"))

    companion object {
        const val LANDLORD_COL_INDEX: Int = 0
        const val ADDRESS_COL_INDEX: Int = 1
        const val CONTACT_INFO_COL_INDEX: Int = 2
        const val LISTED_PROPERTY_COL_INDEX: Int = 3
    }
}
