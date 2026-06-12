package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.NotificationBanner
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
    val inviteJointLandlordLink = Link.byText(page, "invite them to join the property")
    val inviteJointLandlordButton = Button.byText(page, "Invite a joint landlord")

    val inviteJointLandlordIndividualText: Locator
        get() = page.locator("p:has-text('If there are other landlords')")

    val notificationBanner = NotificationBannerPropertyDetailsLandlordView(page)

    val pendingInvitationsSummary: Locator
        get() = page.locator(".govuk-details__summary:has-text('Pending invitations')")

    val pendingInvitationsDetails: Locator
        get() = page.locator("details", Page.LocatorOptions().setHasText("Pending invitations"))

    val cancelInvitationLink = Link.byText(page, "Cancel invitation")

    val expiredInvitationsDetails: Locator
        get() = page.locator("details", Page.LocatorOptions().setHasText("Expired invitations"))

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
