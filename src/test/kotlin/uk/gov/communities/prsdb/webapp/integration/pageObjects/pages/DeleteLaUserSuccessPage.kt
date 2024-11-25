package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getButton
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getConfirmationPageBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class DeleteLaUserSuccessPage(
    page: Page,
) : BasePage(page, "User removed") {
    val confirmationBanner = getConfirmationPageBanner(page)
    private val returnButton = getButton(page)

    fun clickReturnButtonAndAssertNextPage(): ManageLaUsersPage = clickElementAndAssertNextPage(returnButton)
}
