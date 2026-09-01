package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.EnterPasswordStep

// TODO PDJB-1568: Update when enter password page is implemented
class EnterPasswordPage(
    page: Page,
) : BasePage(page, "/${EnterPasswordStep.ROUTE_SEGMENT}") {
    val form = Form(page)
}
