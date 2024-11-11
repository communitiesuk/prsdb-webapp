package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertEquals
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.ConfirmationPageBanner
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class DeleteLaUserSuccessPage(
    page: Page,
) : BasePage(page) {
    val confirmationBanner: ConfirmationPageBanner = ConfirmationPageBanner(page.locator(".govuk-panel--confirmation"))
    private val returnButton: Locator = page.locator("main a")

    override fun validate() {
        assertEquals("User removed", page.title())
    }

    fun returnToManageUsers(): ManageLaUsersPage {
        returnButton.click()
        return createValid(page, ManageLaUsersPage::class)
    }
}
