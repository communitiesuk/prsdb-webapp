package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.NotificationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView

abstract class PropertyDetailsCouncilViewBasePage(
    page: Page,
    urlSegment: String,
) : PropertyDetailsBasePage(page, urlSegment) {
    val notificationBanner = NotificationBanner(page)

    val freeholderSection: Locator
        get() = page.locator("#freeholder-section")

    val freeholderCards: List<PropertyDetailsPageLandlordView.FreeholderSummaryCard>
        get() {
            val locator = page.locator("#freeholder-cards .govuk-summary-card")
            return (0 until locator.count()).map { PropertyDetailsPageLandlordView.FreeholderSummaryCard(locator.nth(it)) }
        }

    val freeholderUpdateLink = Link(page.locator("#freeholder-update-link"))

    val superiorLandlordSection: Locator
        get() = page.locator("#superior-landlord-section")

    val superiorLandlordCards: List<PropertyDetailsPageLandlordView.SuperiorLandlordSummaryCard>
        get() {
            val locator = page.locator("#superior-landlord-cards .govuk-summary-card")
            return (0 until locator.count()).map { PropertyDetailsPageLandlordView.SuperiorLandlordSummaryCard(locator.nth(it)) }
        }

    val superiorLandlordUpdateLink = Link(page.locator("#superior-landlord-update-link"))

    val peopleResponsibleOwnershipSummaryList = PropertyDetailsPageLandlordView.PeopleResponsibleOwnershipSummaryList(page)

    val peopleResponsibleChangeLinks: Locator
        get() = page.locator("#people-responsible a", Page.LocatorOptions().setHasText("Change"))

    val pendingInvitationsDetails: Locator
        get() = page.locator("details", Page.LocatorOptions().setHasText("Pending invitations"))

    val expiredInvitationsDetails: Locator
        get() = page.locator("details", Page.LocatorOptions().setHasText("Expired invitations"))

    val landlordSummaryCards: List<LandlordSummaryCard>
        get() {
            val locator = page.locator("#landlord-summary-cards .govuk-summary-card")
            return (0 until locator.count()).map { LandlordSummaryCard(locator.nth(it)) }
        }

    val propertyManagerSection: Locator
        get() = page.locator("#property-manager-section")

    val propertyManagerUpdateLink = Link(page.locator("#property-manager-update-link"))

    val propertyManagerCard =
        PropertyDetailsPageLandlordView.PropertyManagerSummaryCard(page.locator("#property-manager-section .govuk-summary-card"))

    val propertyManagerNotUsedSummaryList = PropertyDetailsPageLandlordView.PropertyManagerNotUsedSummaryList(page)

    class LandlordSummaryCard(
        locator: Locator,
    ) : SummaryCard(locator) {
        override val summaryList = LandlordCardSummaryList(locator)
    }

    class LandlordCardSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val registrationNumberRow = getRow("Landlord number")
        val emailAddressRow = getRow("Email address")
        val contactNumberRow = getRow("Contact number")
        val contactAddressRow = getRow("Contact address")
    }
}
