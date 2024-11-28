package uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.BaseComponent.Companion.getConfirmationPageBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages.BasePage

class InviteNewLaUserSuccessPage(
    page: Page,
) : BasePage(page, "/invite-new-user/success") {
    val confirmationBanner = getConfirmationPageBanner(page)
}
