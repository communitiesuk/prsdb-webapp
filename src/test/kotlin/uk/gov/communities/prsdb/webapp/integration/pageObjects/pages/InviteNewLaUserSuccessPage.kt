package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getConfirmationPageBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteNewLaUserSuccessPage(
    page: Page,
) : BasePage(page, "/invite-new-user/success") {
    val confirmationBanner = getConfirmationPageBanner(page)
}
