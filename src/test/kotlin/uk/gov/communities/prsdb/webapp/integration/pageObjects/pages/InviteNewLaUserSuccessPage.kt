package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteNewLaUserSuccessPage(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(page, ManageLocalAuthorityUsersController.getLaInviteUserSuccessRoute(urlArguments["localAuthorityId"]!!.toInt())) {
    val confirmationBanner = ConfirmationBanner(page)
    val returnToDashboardButton = Button.byText(page, "Return to dashboard")
}
