package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController
import uk.gov.communities.prsdb.webapp.controllers.ManageUsersViewType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteNewLocalCouncilUserSuccessPage(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        ManageLocalCouncilUsersController.getInviteUserSuccessRoute(
            urlArguments["localCouncilId"]!!.toInt(),
            ManageUsersViewType.LocalAuthorityView,
        ),
    ) {
    val confirmationBanner = ConfirmationBanner(page)
    val returnToDashboardButton = Button.byText(page, "Return to dashboard")
}
