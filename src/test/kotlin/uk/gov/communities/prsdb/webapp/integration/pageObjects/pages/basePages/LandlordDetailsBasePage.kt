package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs

abstract class LandlordDetailsBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val tabs = LandlordDetailsTabs(page)
    val table = Table(page)

    class LandlordDetailsTabs(
        page: Page,
    ) : Tabs(page) {
        fun goToRegisteredProperties() {
            goToTab("Registered properties")
        }
    }
}
