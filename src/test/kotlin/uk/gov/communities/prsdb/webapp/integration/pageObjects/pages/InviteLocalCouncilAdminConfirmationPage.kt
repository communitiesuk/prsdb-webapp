package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteLocalCouncilAdminConfirmationPage(
    page: Page,
) : BasePage(page, ManageLocalCouncilAdminsController.INVITE_LA_ADMIN_CONFIRMATION_ROUTE) {
    val confirmationBanner = ConfirmationBanner(page)
    val returnToDashboardButton = Button.byText(page, "Return to dashboard")
    val inviteAnotherUserButton = Button.byText(page, "Invite another user")
}
