package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CANCEL_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CancelLocalCouncilUserInvitationBasePage

class CancelLocalCouncilUserInvitationPage(
    page: Page,
) : CancelLocalCouncilUserInvitationBasePage(page, "/$CANCEL_INVITATION_PATH_SEGMENT")
