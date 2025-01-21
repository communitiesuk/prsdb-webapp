package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LandlordDetailsPage(
    page: Page,
) : BasePage(page, "/landlord-details") {
    val tabs = Tabs(page)

    fun getActiveTabPanelId() = tabs.activePanel?.getAttribute("id")

    fun goToRegisteredProperties() {
        tabs.goToTab(1)
    }
}
