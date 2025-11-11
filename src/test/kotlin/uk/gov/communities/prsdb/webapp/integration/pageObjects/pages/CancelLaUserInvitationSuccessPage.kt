package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CancelLaUserInvitationSuccessBasePage

class CancelLaUserInvitationSuccessPage(
    page: Page,
    urlArguments: Map<String, String>,
) : CancelLaUserInvitationSuccessBasePage(
        page,
        ManageLocalCouncilUsersController.getLaCancelInviteSuccessRoute(
            urlArguments["localAuthorityId"]!!.toInt(),
            urlArguments["invitationId"]!!.toLong(),
        ),
    )
