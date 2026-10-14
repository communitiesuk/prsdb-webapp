package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.ConfirmationStep

class ConfirmationPage(
    page: Page,
) : BasePage(page, "$ACCEPT_INVITATION_ROUTE/${ConfirmationStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val joinedOrganisation = page.locator("#joined-organisation")
    val confirmationEmail = page.locator("#confirmation-email")
    val accessLevel = page.locator("#access-level")
    val permissions = page.locator("#permissions li")
    val goToDashboardLink = Link(page.locator("#go-to-dashboard"))
}
