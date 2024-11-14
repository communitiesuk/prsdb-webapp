package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertEquals
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class EditLaUserPage(
    page: Page,
) : BasePage(page) {
    val userName: Locator = header
    val email: Locator = page.locator("main header p")
    val basicRadio: Locator = page.locator("form input[value='false']")
    val adminRadio: Locator = page.locator("form input[value='true']")
    private val submitButton = page.locator("button[type=\"submit\"]").filter(Locator.FilterOptions().apply { hasText = "Save" })
    private val deleteButton = page.locator("form a").filter(Locator.FilterOptions().apply { hasText = "Remove" })

    override fun validate() {
        assertEquals("Manage Local Authority Users", page.title())
    }

    fun accessLevel(): AccessLevelSelection {
        val selectedValue = page.locator("form input[type='radio']:checked").getAttribute("value")
        return when (selectedValue) {
            "false" -> AccessLevelSelection.BASIC
            "true" -> AccessLevelSelection.ADMIN
            else -> AccessLevelSelection.NONE
        }
    }

    fun selectAccessLevel(level: AccessLevelSelection) {
        when (level) {
            AccessLevelSelection.BASIC -> basicRadio.click()
            AccessLevelSelection.ADMIN -> adminRadio.click()
            AccessLevelSelection.NONE -> throw IllegalArgumentException("Cannot set access level to NONE")
        }
    }

    fun submit(): ManageLaUsersPage {
        submitButton.click()
        return createValid(page, ManageLaUsersPage::class)
    }

    fun deleteUser(): ConfirmDeleteLaUserPage {
        deleteButton.click()
        return createValid(page, ConfirmDeleteLaUserPage::class)
    }

    enum class AccessLevelSelection {
        BASIC,
        ADMIN,
        NONE,
    }
}
