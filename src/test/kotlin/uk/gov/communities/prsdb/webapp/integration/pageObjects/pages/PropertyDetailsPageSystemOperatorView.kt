package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLocalCouncilView.LandlordSummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PropertyDetailsBasePage

class PropertyDetailsPageSystemOperatorView(
    page: Page,
    urlArguments: Map<String, String>,
) : PropertyDetailsBasePage(
        page,
        PropertyDetailsController.getPropertyDetailsForSystemOperatorPath(urlArguments["propertyOwnershipId"]!!.toLong()),
    ) {
    val landlordSummaryCards: List<LandlordSummaryCard>
        get() {
            val locator = page.locator("#landlord-summary-cards .govuk-summary-card")
            return (0 until locator.count()).map { LandlordSummaryCard(locator.nth(it)) }
        }
}
