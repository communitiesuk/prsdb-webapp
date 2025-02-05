package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PropertyDetailsPage(
    page: Page,
) : BasePage(page, "/property-details") {
    val tabs = Tabs(page, 2)

    fun getActiveTabPanelId() = tabs.getActiveTabPanelId()

    fun goToLandlordDetails() {
        tabs.goToTab("Landlord details")
    }

    fun goToPropertyDetails() {
        tabs.goToTab("Property details")
    }

    fun clickLandlordNameLink(landlordName: String) {
        getLink(page, landlordName).click()
    }
}
