package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CancelLaUserInvitationSuccessPage(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        ManageLocalAuthorityUsersController.getLaCancelInviteSuccessRoute(
            urlArguments["localAuthorityId"]!!.toInt(),
            urlArguments["invitationId"]!!.toLong(),
        ),
    ) {
    val confirmationBanner = ConfirmationBanner(page)
    val returnButton = Button.byText(page, "Return to manage users")
}
