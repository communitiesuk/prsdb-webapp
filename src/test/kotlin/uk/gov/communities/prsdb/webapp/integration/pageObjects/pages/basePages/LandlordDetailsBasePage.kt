package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs

abstract class LandlordDetailsBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val tabs = LandlordDetailsTabs(page)
    val personalDetailsSummaryList = LandlordPersonalDetailsSummaryList(page)
    val registeredPropertiesTable = Table(page)

    class LandlordDetailsTabs(
        page: Page,
    ) : Tabs(page) {
        fun goToRegisteredProperties() {
            goToTab("Registered properties")
        }
    }

    class LandlordPersonalDetailsSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val nameRow = getRow("Name")
        val emailRow = getRow("Email address")
    }
}
