package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.InvalidLinkStep

class InvalidLinkPage(
    page: Page,
) : BasePage(page, "$ACCEPT_INVITATION_ROUTE/${InvalidLinkStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val alreadyRespondedText: Locator = page.locator("main p.govuk-body").first()
    val signInLink = Link.byText(page, "sign in to your account")
    val bulletPoints: Locator = page.locator("main ul.govuk-list--bullet li")
    val stillNeedAccessText: Locator = page.locator("main h2 + p.govuk-body")
}
