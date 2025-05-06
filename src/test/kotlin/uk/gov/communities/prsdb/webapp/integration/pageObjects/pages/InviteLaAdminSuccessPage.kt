package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.InviteLocalAuthorityAdminController
import uk.gov.communities.prsdb.webapp.controllers.InviteLocalAuthorityAdminController.Companion.INVITE_LA_ADMIN_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteLaAdminSuccessPage(
    page: Page,
) : BasePage(page, "${InviteLocalAuthorityAdminController.INVITE_LA_ADMIN_ROUTE}/$CONFIRMATION_PATH_SEGMENT") {
    val confirmationBanner = ConfirmationBanner(page)
    val returnToDashboardButton = Button.byText(page, "Return to dashboard")
}
