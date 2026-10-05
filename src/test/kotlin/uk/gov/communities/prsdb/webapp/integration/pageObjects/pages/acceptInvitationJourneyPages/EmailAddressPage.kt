package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.EmailAddressStep

class EmailAddressPage(
    page: Page,
) : BasePage(page, "$ACCEPT_INVITATION_ROUTE/${EmailAddressStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = PostForm(page)
}
