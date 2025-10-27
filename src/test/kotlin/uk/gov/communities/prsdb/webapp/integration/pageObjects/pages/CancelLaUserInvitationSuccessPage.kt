package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CancelLaUserInvitationSuccessBasePage

class CancelLaUserInvitationSuccessPage(
    page: Page,
    urlArguments: Map<String, String>,
) : CancelLaUserInvitationSuccessBasePage(
        page,
        ManageLocalAuthorityUsersController.getLaCancelInviteSuccessRoute(
            urlArguments["localAuthorityId"]!!.toInt(),
            urlArguments["invitationId"]!!.toLong(),
        ),
    )
