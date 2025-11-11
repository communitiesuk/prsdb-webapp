package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CANCEL_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CancelLaUserInvitationSuccessBasePage

class CancelLaAdminInvitationSuccessPage(
    page: Page,
    urlArguments: Map<String, String>,
) : CancelLaUserInvitationSuccessBasePage(
        page,
        "$SYSTEM_OPERATOR_PATH_SEGMENT/$CANCEL_INVITATION_PATH_SEGMENT/${urlArguments["invitationId"]}/$CONFIRMATION_PATH_SEGMENT",
    )
