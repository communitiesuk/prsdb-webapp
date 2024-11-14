package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class ConfirmDeleteLaUserPage(
    page: Page,
) : BasePage(page) {
    private val userDetailsSection = page.locator("section").nth(0)
    private val deleteButton = page.locator("button[type='submit']")

    override fun validate() {
        assertThat(header).containsText("Before you remove this account")
    }

    fun assertUserNameVisible(name: String) {
        assertThat(userDetailsSection).containsText(name)
    }

    fun assertEmailVisible(email: String) {
        assertThat(userDetailsSection).containsText(email)
    }

    fun deleteUser(): DeleteLaUserSuccessPage {
        deleteButton.click()
        return createValid(page, DeleteLaUserSuccessPage::class)
    }
}
