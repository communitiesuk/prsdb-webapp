package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getChildComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SearchRegisterBasePage

class SearchPropertyRegisterPage(
    page: Page,
) : SearchRegisterBasePage(page, "/search/property") {
    fun getPropertyLink(rowIndex: Int) = getChildComponent(getResultTable().getCell(rowIndex, PROPERTY_COL_INDEX), "a")

    fun getLandlordLink(rowIndex: Int) = getChildComponent(getResultTable().getCell(rowIndex, PROPERTY_LANDLORD_COL_INDEX), "a")

    companion object {
        const val PROPERTY_COL_INDEX: Int = 0
        const val REG_NUM_COL_INDEX: Int = 1
        const val LA_COL_INDEX: Int = 2
        const val PROPERTY_LANDLORD_COL_INDEX: Int = 3
    }
}
