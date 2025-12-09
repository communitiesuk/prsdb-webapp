package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CancelLocalCouncilUserInvitationSuccessBasePage

class CancelLocalCouncilUserInvitationSuccessPage(
    page: Page,
    urlArguments: Map<String, String>,
) : CancelLocalCouncilUserInvitationSuccessBasePage(
        page,
        ManageLocalCouncilUsersController.getLocalCouncilCancelInviteSuccessRoute(
            urlArguments["localCouncilId"]!!.toInt(),
            urlArguments["invitationId"]!!.toLong(),
        ),
    )
