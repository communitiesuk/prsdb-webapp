package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.COMPLIANCE_ACTIONS_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.InsetText
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Pagination
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ComplianceActionsPage(
    page: Page,
) : BasePage(page, COMPLIANCE_ACTIONS_URL) {
    val heading = Heading.default(page)
    val hintText = Heading(page.locator("p.govuk-hint"))
    val bodyText = Heading(page.locator("header p.govuk-body"))
    val insetText = InsetText(page)
    val pagination = Pagination(page)

    fun getSummaryCard(propertyAddress: String) = ComplianceActionSummaryCard(page, propertyAddress)

    fun getRedesignedSummaryCard(propertyAddress: String) = RedesignedComplianceActionSummaryCard(page, propertyAddress)

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

    class RedesignedComplianceActionSummaryCard(
        page: Page,
        title: String,
    ) : SummaryCard(page, title) {
        override val summaryList = RedesignedComplianceActionSummaryList(locator)
        val epcInsetText = InsetText(locator)
    }

    class RedesignedComplianceActionSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val statusRow = getRow("Status")
        val registrationNumRow = getRow("Property Registration Number")
        val gasSafetyRow = getRow("Gas safety certificate")
        val electricalSafetyRow = getRow("Electrical safety certificate")
        val energyPerformanceRow = getRow("Energy performance certificate")
    }
}
