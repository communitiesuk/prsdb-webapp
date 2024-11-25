package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getSection
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getSubmitButton
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmDeleteLaUserPage(
    page: Page,
) : BasePage(page, "Remove a user") {
    val userDetailsSection = getSection(page)
    private val deleteAccountButton = getSubmitButton(page)

    fun clickDeleteAccountButtonAndAssertNextPage(): DeleteLaUserSuccessPage = clickElementAndAssertNextPage(deleteAccountButton)
}
