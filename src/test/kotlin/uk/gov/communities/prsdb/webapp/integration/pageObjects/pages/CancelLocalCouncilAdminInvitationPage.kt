package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CANCEL_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CancelLocalCouncilUserInvitationBasePage

class CancelLocalCouncilAdminInvitationPage(
    page: Page,
    urlArguments: Map<String, String>,
) : CancelLocalCouncilUserInvitationBasePage(
        page,
        "$SYSTEM_OPERATOR_PATH_SEGMENT/$CANCEL_INVITATION_PATH_SEGMENT/${urlArguments["invitationId"]}",
    ) {
    val backLink = BackLink.default(page)
}
