package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CANCEL_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SUCCESS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CancelLaUserInvitationSuccessPage(
    page: Page,
) : BasePage(page, "/$CANCEL_INVITATION_PATH_SEGMENT/$SUCCESS_PATH_SEGMENT") {
    val confirmationBanner = ConfirmationBanner(page)
    val returnButton = Button.default(page)
}
