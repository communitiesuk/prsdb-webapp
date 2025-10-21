package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CANCEL_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CancelLaUserInvitationBasePage

class CancelLaAdminInvitationPage(
    page: Page,
) : CancelLaUserInvitationBasePage(page, "$SYSTEM_OPERATOR_PATH_SEGMENT/$CANCEL_INVITATION_PATH_SEGMENT")
