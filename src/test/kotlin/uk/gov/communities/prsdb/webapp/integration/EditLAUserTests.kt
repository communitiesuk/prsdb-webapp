package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.ACCESS_LEVEL_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.USERNAME_COL_INDEX
import kotlin.test.assertEquals

class EditLAUserTests : IntegrationTest() {
    @Test
    fun `a user's access level can be updated`() {
        // There is a basic user called Arthur Dent
        var manageUsersPage = navigator.goToManageLaUsers(1)
        assertThat(manageUsersPage.table.getCell(0, USERNAME_COL_INDEX)).containsText("Arthur Dent")
        assertThat(manageUsersPage.table.getCell(0, ACCESS_LEVEL_COL_INDEX)).containsText("Basic")

        // Editing the user shows Arthur Dent's page
        var editUserPage = manageUsersPage.clickChangeLinkAndAssertNextPage(0)
        assertThat(editUserPage.name).containsText("Arthur Dent")
        // TODO PRSD-405: fix when LA users have email addresses
        assertThat(editUserPage.email).containsText("Arthur Dent@Betelgeuse.gov.uk")
        assertEquals("false", editUserPage.isManagerRadios.getSelectedValue())

        // Update the user's access level to admin
        editUserPage.isManagerRadios.getRadio("true").click()
        manageUsersPage = editUserPage.submitFormAndAssertNextPage()

        // The user is now shown as an admin in the table
        assertThat(manageUsersPage.table.getCell(0, USERNAME_COL_INDEX)).containsText("Arthur Dent")
        assertThat(manageUsersPage.table.getCell(0, ACCESS_LEVEL_COL_INDEX)).containsText("Admin")

        // The user's page also now defaults to admin
        editUserPage = manageUsersPage.clickChangeLinkAndAssertNextPage(0)
        assertEquals("true", editUserPage.isManagerRadios.getSelectedValue())
    }

    @Test
    fun `a user can be deleted`() {
        // Edit Arthur Dent
        var manageUsersPage = navigator.goToManageLaUsers(1)
        assertThat(manageUsersPage.table.getCell(0, USERNAME_COL_INDEX)).containsText("Arthur Dent")
        val editUserPage = manageUsersPage.clickChangeLinkAndAssertNextPage(0)

        // Delete the user
        val confirmDeletePage = editUserPage.clickRemoveAccountButtonAndAssertNextPage()
        assertThat(confirmDeletePage.userDetailsSection).containsText("Arthur Dent")
        // TODO PRSD-405: fix when LA users have email addresses
        assertThat(confirmDeletePage.userDetailsSection).containsText("Arthur Dent@Betelgeuse.gov.uk")
        val successPage = confirmDeletePage.clickDeleteAccountButtonAndAssertNextPage()

        // The success page confirms the user is deleted
        assertThat(successPage.confirmationBanner).containsText("You've removed Arthur Dent's account from Betelgeuse")
        manageUsersPage = successPage.clickReturnButtonAndAssertNextPage()

        // The user is no longer in the table
        assertThat(manageUsersPage.table.getCell(0, USERNAME_COL_INDEX)).not().containsText("Arthur Dent")
    }
}
