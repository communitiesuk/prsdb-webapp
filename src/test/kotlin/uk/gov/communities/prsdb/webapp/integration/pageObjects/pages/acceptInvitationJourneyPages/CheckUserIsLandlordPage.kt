package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.CheckUserIsLandlordStep

class CheckUserIsLandlordPage(
    page: Page,
) : BasePage(page, "$ACCEPT_INVITATION_ROUTE/${CheckUserIsLandlordStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = PostForm(page)
}
