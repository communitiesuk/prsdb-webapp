package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getButton
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getConfirmationPageBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class DeleteLaUserSuccessPage(
    page: Page,
) : BasePage(page, "/delete-user/success") {
    val confirmationBanner = getConfirmationPageBanner(page)
    val returnButton = getButton(page)
}
