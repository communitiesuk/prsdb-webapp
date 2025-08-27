package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.COMPLIANCE_ACTIONS_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ComplianceActionsPage(
    page: Page,
) : BasePage(page, COMPLIANCE_ACTIONS_URL) {
    val heading = Heading.default(page)
    val hintText = Heading(page.locator("p.govuk-hint"))
    val viewRegisteredPropertiesLink = Link.byText(page, "View your property records")

    fun getSummaryCard(propertyAddress: String) = ComplianceActionSummaryCard(page, propertyAddress)

    class ComplianceActionSummaryCard(
        page: Page,
        title: String,
    ) : SummaryCard(page, title) {
        override val summaryList = ComplianceActionSummaryList(locator)
    }

    class ComplianceActionSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val registrationNumRow = getRow("Registration number")
        val gasSafetyRow = getRow("Gas safety")
        val electricalSafetyRow = getRow("Electrical safety")
        val energyPerformanceRow = getRow("Energy performance")
    }
}
