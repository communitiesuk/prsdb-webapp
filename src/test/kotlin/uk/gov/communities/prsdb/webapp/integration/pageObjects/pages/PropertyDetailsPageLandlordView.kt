package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.NotificationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
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

    val notificationBanner = NotificationBannerPropertyDetailsLandlordView(page)

    val landlordsTab = LandlordsTab(page)

    class NotificationBannerPropertyDetailsLandlordView(
        page: Page,
    ) : NotificationBanner(page) {
        val updateMissingGasSafetyLink = Link.byText(page, "Upload a certificate")
        val updateMissingEicrLink = Link.byText(page, "Upload an EICR")
        val addEpcLink = Link.byText(page, "Add a new certificate")
        val updateExpiredGasSafetyLink = Link.byText(page, "Upload a new certificate")
        val updateExpiredEicrLink = Link.byText(page, "Upload a new EICR")
        val addEpcOrMeesExemptionLink = Link.byText(page, "add a new certificate or add a MEES exemption")
        val addComplianceInformationLink = Link.byText(page, "Add compliance information")
    }

    class LandlordsTab(
        private val page: Page,
    ) {
        val registeredLandlordsHeading = Heading(page.locator("#landlord-details h3").first())
        val inviteJointLandlordButton = page.locator("#landlord-details a", Page.LocatorOptions().setHasText("Invite a joint landlord"))
        val confirmSoleLandlordLink = Link.byText(page, "confirm that you’re the only landlord")
        val pendingInvitationsDetails = page.locator("#landlord-details details", Page.LocatorOptions().setHasText("Pending invitations"))
        val expiredInvitationsDetails = page.locator("#landlord-details details", Page.LocatorOptions().setHasText("Expired invitations"))
        val joinRequestsHeading = page.locator("#landlord-details h3", Page.LocatorOptions().setHasText("Joint landlord requests"))
        val joinRequestsBanner = page.locator("#join-requests-banner")

        fun landlordCard(index: Int = 0) = SummaryCard(page.locator("#landlord-details .govuk-summary-card").nth(index))
    }
}
