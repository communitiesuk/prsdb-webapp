package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PropertyDetailsPage(
    page: Page,
) : BasePage(page, "/property-details") {
    val tabs = Tabs(page, 2)

    // TODO PRSD-719 this will need to look for the second summary list on the page
    private val summaryList = SummaryList(page)

    val nameRowValue = summaryList.getRowValue(0)

    fun getActiveTabPanelId() = tabs.getActiveTabPanelId()

    fun goToLandlordDetails() {
        tabs.goToTab("Landlord details")
    }

    fun goToPropertyDetails() {
        tabs.goToTab("Property details")
    }
}
