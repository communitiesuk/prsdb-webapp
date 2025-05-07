package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.InviteLocalAuthorityAdminController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteLaAdminConfirmationPage(
    page: Page,
) : BasePage(page, InviteLocalAuthorityAdminController.INVITE_LA_ADMIN_CONFIRMATION_ROUTE) {
    val confirmationBanner = ConfirmationBanner(page)
    val returnToDashboardButton = Button.byText(page, "Return to dashboard")
    val inviteAnotherUserButton = Button.byText(page, "Invite another user")
}
