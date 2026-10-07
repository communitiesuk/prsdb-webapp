package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.CheckAnswersStep

class CheckAnswersPage(
    page: Page,
) : BasePage(page, "$ACCEPT_INVITATION_ROUTE/${CheckAnswersStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val backLink = BackLink.default(page)
    val form = PostForm(page)
    val summaryList = CheckAnswersSummaryList(page)

    class CheckAnswersSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val organisationRow = getRow("Organisation")
        val nameRow = getRow("Name")
        val emailRow = getRow("Email address")
    }
}
