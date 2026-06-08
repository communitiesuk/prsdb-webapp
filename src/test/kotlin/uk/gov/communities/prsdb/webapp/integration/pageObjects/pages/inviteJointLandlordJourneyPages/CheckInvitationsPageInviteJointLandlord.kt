package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.InviteJointLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord.CheckInvitationsStep

class CheckInvitationsPageInviteJointLandlord(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        InviteJointLandlordController.getInviteJointLandlordRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${CheckInvitationsStep.ROUTE_SEGMENT}",
    ) {
    val form = Form(page)
    val summaryName = Heading(page.locator("#summary-name"))
    val summaryList = CheckInvitationsSummaryList(page)

    fun confirm() = form.submit()

    class CheckInvitationsSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val invitationsRow = getRow(0)
    }
}
