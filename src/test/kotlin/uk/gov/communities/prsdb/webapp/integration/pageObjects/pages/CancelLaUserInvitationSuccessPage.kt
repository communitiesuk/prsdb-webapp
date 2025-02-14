package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getConfirmationPageBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CancelLaUserInvitationSuccessPage(
    page: Page,
) : BasePage(page, "/cancel-invitation/success") {
    val confirmationBanner = getConfirmationPageBanner(page)
    val returnButton = Button.byText(page)
}
