package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.StoreAccessStep

// TODO PDJB-1659: Update when store access for letting agent page is implemented
class StoreAccessPage(
    page: Page,
) : BasePage(page, "/${StoreAccessStep.ROUTE_SEGMENT}") {
    val form = Form(page)
}
