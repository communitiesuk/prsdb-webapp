package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CANCEL_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CancelLaUserInvitationBasePage

class CancelLaUserInvitationPage(
    page: Page,
) : CancelLaUserInvitationBasePage(page, "/$CANCEL_INVITATION_PATH_SEGMENT")
