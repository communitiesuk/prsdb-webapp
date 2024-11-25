package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getConfirmationPageBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteNewLaUserSuccessPage(
    page: Page,
) : BasePage(page, "Invite sent") {
    val confirmationBanner = getConfirmationPageBanner(page)
}
