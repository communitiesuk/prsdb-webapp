package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs

abstract class LandlordDetailsBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val tabs = Tabs(page)
    val table = Table(page)

    fun getActiveTabPanelId() = tabs.getActiveTabPanelId()

    fun goToRegisteredProperties() {
        tabs.goToTab("Registered properties")
    }
}
