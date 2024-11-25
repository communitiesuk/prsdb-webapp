package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent.Companion.getButton
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent.Companion.getConfirmationPageBanner
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class DeleteLaUserSuccessPage(
    page: Page,
) : BasePage(page, "User removed") {
    val confirmationBanner = getConfirmationPageBanner(page)
    private val returnButton = getButton(page)

    fun clickReturnButtonAndAssertNextPage(): ManageLaUsersPage = clickElementAndAssertNextPage(returnButton)
}
