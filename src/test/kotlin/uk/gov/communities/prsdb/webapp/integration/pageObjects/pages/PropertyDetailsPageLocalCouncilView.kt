package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.NotificationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PropertyDetailsBasePage

class PropertyDetailsPageLocalCouncilView(
    page: Page,
    urlArguments: Map<String, String>,
) : PropertyDetailsBasePage(
        page,
        PropertyDetailsController.getPropertyDetailsPath(
            urlArguments["propertyOwnershipId"]!!.toLong(),
            isLocalCouncilView = true,
        ),
    ) {
    val notificationBanner = NotificationBanner(page)

    val landlordSummaryCards: List<LandlordSummaryCard>
        get() {
            val count = page.locator("#landlord-details .govuk-summary-card").count()
            return (0 until count).map { LandlordSummaryCard(page.locator("#landlord-details .govuk-summary-card").nth(it)) }
        }

    class LandlordSummaryCard(
        locator: Locator,
    ) : SummaryCard(locator) {
        override val summaryList = LandlordCardSummaryList(locator)
    }

    class LandlordCardSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val registrationNumberRow = getRow("Landlord Registration Number")
        val emailAddressRow = getRow("Email address")
        val contactNumberRow = getRow("Contact number")
        val contactAddressRow = getRow("Contact address")
    }
}
