package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.INCOMPLETE_PROPERTIES_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LandlordIncompletePropertiesPage(
    page: Page,
) : BasePage(page, INCOMPLETE_PROPERTIES_URL) {
    val heading = Heading(page.locator("h1.govuk-heading-l"))
    val subHeading = Heading(page.locator("p.govuk-body-l"))
    val text = Heading(page.locator("p.govuk-body"))
    val viewRegisteredPropertiesLink = Link.byText(page, "view registered properties")
    val registerANewPropertyLinks = Link.byText(page, "register a new property")

    val firstSummaryCard = SummaryCard(page.locator("html"), 0)
    val secondSummaryCard = SummaryCard(page.locator("html"), 1)

    val firstSummaryCardList = IncompletePropertiesSummaryCardList(firstSummaryCard.summaryCardLocator)
    val secondSummaryCardList = IncompletePropertiesSummaryCardList(secondSummaryCard.summaryCardLocator)

    class IncompletePropertiesSummaryCardList(
        locator: Locator,
    ) : SummaryList(locator) {
        val propertyAddressRow = getRow("Property address")
        val localAuthorityRow = getRow("Local authority")
        val completeByRow = getRow("Complete by")
    }
}
