package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ErrorBasePage

class InvalidLinkPageLettingAgentInvitation(
    page: Page,
) : ErrorBasePage(page, LettingAgentInvitationController.LETTING_AGENT_INVITATION_INVALID_LINK_ROUTE)
