package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.ConfirmationStep

// TODO PDJB-1567: Update when password creation confirmation page is implemented
class PasswordCreationConfirmationPage(
    page: Page,
) : BasePage(page, "/${ConfirmationStep.ROUTE_SEGMENT}") {
    val form = Form(page)
}
