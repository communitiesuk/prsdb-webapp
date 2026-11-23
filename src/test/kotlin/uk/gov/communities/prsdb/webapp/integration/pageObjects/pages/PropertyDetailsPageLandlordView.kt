package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.NotificationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TicketPanel
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PropertyDetailsBasePage

class PropertyDetailsPageLandlordView(
    page: Page,
    urlArguments: Map<String, String>,
) : PropertyDetailsBasePage(
        page,
        PropertyDetailsController.getPropertyDetailsPath(
            urlArguments["propertyOwnershipId"]!!.toLong(),
            isLocalCouncilView = false,
        ),
    ) {
    val deregisterPropertyLink = Link.byText(page, "Deregister property")
    val deactivatePropertyLink = Link.byText(page, "Deactivate this property")

    val deactivationSuccessBanner: Locator
        get() = page.locator("#deactivation-success-banner")

    val delegateToLettingAgentLink = Link.byText(page, "Allow your letting agent or property manager to update details")
    val removeLettingAgentLink = Link.byText(page, "Remove your letting agent or property manager")
    val inviteJointLandlordLink = Link.byText(page, "invite them to join the property")
    val inviteJointLandlordButton = Button.byText(page, "Invite a joint landlord")

    val caption: Locator
        get() = page.locator("main .govuk-caption-l")

    val reactivatePropertyPanel = TicketPanel(page.locator("#reactivate-property-panel"))

    val reactivatePropertyButton = Button.byText(page, "Reactivate this property")

    val markAsSingleLandlordInsetText: Locator
        get() = page.locator(".govuk-inset-text:has-text(\"confirm that you're the only landlord\")")

    val switchToIndividualLink = Link.byText(page, "confirm that you're the only landlord")

    val inviteJointLandlordIndividualText: Locator
        get() = page.locator("p:has-text('If there are other landlords')")

    val notificationBanner = NotificationBannerPropertyDetailsLandlordView(page)

    val freeholderSection: Locator
        get() = page.locator("#freeholder-section")

    val freeholderCards: List<FreeholderSummaryCard>
        get() {
            val locator = page.locator("#freeholder-cards .govuk-summary-card")
            return (0 until locator.count()).map { FreeholderSummaryCard(locator.nth(it)) }
        }

    val freeholderUpdateLink = Link(page.locator("#freeholder-update-link"))

    val superiorLandlordSection: Locator
        get() = page.locator("#superior-landlord-section")

    val superiorLandlordCards: List<SuperiorLandlordSummaryCard>
        get() {
            val locator = page.locator("#superior-landlord-cards .govuk-summary-card")
            return (0 until locator.count()).map { SuperiorLandlordSummaryCard(locator.nth(it)) }
        }

    val superiorLandlordUpdateLink = Link(page.locator("#superior-landlord-update-link"))

    val noSuperiorLandlordsSummaryList = NoSuperiorLandlordsSummaryList(page)

    val pendingInvitationsSummary: Locator
        get() = page.locator(".govuk-details__summary:has-text('Pending invitations')")

    val pendingInvitationsDetails: Locator
        get() = page.locator("details", Page.LocatorOptions().setHasText("Pending invitations"))

    val cancelInvitationLink = Link.byText(page, "Cancel invitation")

    val resendInvitationLink = Link.byText(page, "Send a new email invitation")

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

    val propertyManagerCard = PropertyManagerSummaryCard(page.locator("#property-manager-section .govuk-summary-card"))

    val propertyManagerNotUsedSummaryList = PropertyManagerNotUsedSummaryList(page)

    val peopleResponsibleOwnershipSummaryList = PeopleResponsibleOwnershipSummaryList(page)

    class PeopleResponsibleOwnershipSummaryList(
        page: Page,
    ) : SummaryList(page.locator("#people-responsible")) {
        val ownsPropertyRow = getRow("Do you own this property?")
        val ownershipTypeRow = getRow("Ownership type")
        val freeholdTypeRow = getRow("Freehold type")
    }

    class LandlordSummaryCard(
        locator: Locator,
    ) : SummaryCard(locator) {
        override val summaryList = LandlordCardSummaryList(locator)
    }

    class FreeholderSummaryCard(
        locator: Locator,
    ) : SummaryCard(locator, headingLevel = "h4") {
        val bodyText = locator.locator(".govuk-summary-card__content p")
        val actionLinks = locator.locator(".govuk-summary-card__action")
    }

    class SuperiorLandlordSummaryCard(
        locator: Locator,
    ) : SummaryCard(locator, headingLevel = "h4") {
        val bodyText = locator.locator(".govuk-summary-card__content p")
        val actionLinks = locator.locator(".govuk-summary-card__action")
    }

    class NoSuperiorLandlordsSummaryList(
        page: Page,
    ) : SummaryList(page.locator("#superior-landlord-section")) {
        val anyoneInOwnershipChainRow = getRow("Is there anyone else in the property’s ownership chain?")
    }

    class LandlordCardSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val registrationNumberRow = getRow("Landlord number")
        val emailAddressRow = getRow("Email address")
    }

    class PropertyManagerSummaryCard(
        locator: Locator,
    ) : SummaryCard(locator, headingLevel = "h4") {
        override val summaryList = PropertyManagerCardSummaryList(locator)
        val actionLinks: Locator = locator.locator(".govuk-summary-card__action")
    }

    class PropertyManagerCardSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val emailAddressRow = getRow("Email address")
    }

    class PropertyManagerNotUsedSummaryList(
        page: Page,
    ) : SummaryList(page.locator("#property-manager-section")) {
        val usingPropertyManagerRow = getRow("Are you using a property manager?")
    }

    class NotificationBannerPropertyDetailsLandlordView(
        page: Page,
    ) : NotificationBanner(page) {
        val viewComplianceCertificatesLink =
            Link.byText(
                page,
                "View compliance certificates",
                selectorOrLocator = ".govuk-notification-banner__link",
            )
    }
}
