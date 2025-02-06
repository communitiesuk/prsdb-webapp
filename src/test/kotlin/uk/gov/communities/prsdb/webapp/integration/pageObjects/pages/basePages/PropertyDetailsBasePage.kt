package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs

abstract class PropertyDetailsBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
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

    fun clickBackLink() {
        getLink(page, "Back").click()
    }
}
