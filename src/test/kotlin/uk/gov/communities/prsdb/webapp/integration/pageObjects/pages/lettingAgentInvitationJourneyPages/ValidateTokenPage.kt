package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.ValidateTokenStep

// TODO PDJB-1659: Update when validate token step is replaced by an interceptor
class ValidateTokenPage(
    page: Page,
) : BasePage(page, "/${ValidateTokenStep.ROUTE_SEGMENT}") {
    val form = Form(page)
}
