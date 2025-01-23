package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LandlordDetailsPage(
    page: Page,
) : BasePage(page, "/landlord-details") {
    val tabs = Tabs(page, 2)
    val table = Table(page)

    fun getActiveTabPanelId() = tabs.getActiveTabPanelId()

    fun goToRegisteredProperties() {
        tabs.goToTab("Registered properties")
    }
}
