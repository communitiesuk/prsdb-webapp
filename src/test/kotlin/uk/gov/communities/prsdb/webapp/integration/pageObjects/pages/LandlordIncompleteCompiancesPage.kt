package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.INCOMPLETE_COMPLIANCES_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LandlordIncompleteCompiancesPage(
    page: Page,
) : BasePage(page, INCOMPLETE_COMPLIANCES_URL) {
    val heading = Heading(page.locator("h1.govuk-heading-l"))
    val hintText = Heading(page.locator("p.govuk-hint"))
    val viewRegisteredPropertiesLink = Link.byText(page, "View your property records")

    val firstSummaryCard = IncompletePropertySummaryCard(page, 0)
    val secondSummaryCard = IncompletePropertySummaryCard(page, 1)

    class IncompletePropertySummaryCard(
        parentLocator: Locator,
        index: Int,
    ) : SummaryCard(parentLocator, index) {
        constructor(page: Page, index: Int) : this(page.locator("html"), index)

        override val summaryCardList = IncompletePropertiesSummaryCardList(locator)

        val continueLink = this.actions("Continue").actionLink

        val startLink = this.actions("Start").actionLink

        class IncompletePropertiesSummaryCardList(
            locator: Locator,
        ) : SummaryList(locator) {
            val propertyAddressRow = getRow("Property address")
            val localAuthorityRow = getRow("Local council")
            val certificatesDueRow = getRow("Certificates due")
            val gasSafetyRow = getRow("Gas safety")
            val electricalSafetyRow = getRow("Electrical safety")
            val energyPerformanceRow = getRow("Energy performance")
            val landlordResponsibilitiesRow = getRow("Landlord responsibilities")
        }
    }
}
