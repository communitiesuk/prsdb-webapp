package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.SetPasswordStep

// TODO PDJB-1566: Update when set password page is implemented
class SetPasswordPage(
    page: Page,
) : BasePage(page, "/${SetPasswordStep.ROUTE_SEGMENT}") {
    val form = Form(page)
}
