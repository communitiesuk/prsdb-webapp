package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.INVALID_LINK_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InvalidLinkPageLettingAgentInvitation(
    page: Page,
) : BasePage(page, "/$INVALID_LINK_PAGE_PATH_SEGMENT")
