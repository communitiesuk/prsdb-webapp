package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.EditLaUserPage
import kotlin.test.assertEquals

@Sql("/data-local.sql")
class EditLAUserTests : IntegrationTest() {
    @Test
    fun `a user's access level can be updated`() {
        // There is a basic user called Arthur Dent
        var manageUsersPage = navigator.goToManageLaUsers(1)
        var row = manageUsersPage.table.row(0)
        assertTrue(row.username().contains("Arthur Dent"))
        assertTrue(row.accessLevel().contains("Basic"))

        // Editing the user shows Arthur Dent's page
        var editUserPage = row.editUser()
        assertThat(editUserPage.userName).containsText("Arthur Dent")
        assertThat(editUserPage.email).containsText("Arthur Dent") // TODO PRSD-405: fix when LA users have email addresses
        assertEquals(EditLaUserPage.AccessLevelSelection.BASIC, editUserPage.accessLevel())

        // Update the user's access level to admin
        editUserPage.selectAccessLevel(EditLaUserPage.AccessLevelSelection.ADMIN)
        manageUsersPage = editUserPage.submit()

        // The user is now shown as an admin in the table
        row = manageUsersPage.table.row(0)
        assertTrue(row.username().contains("Arthur Dent"))
        assertTrue(row.accessLevel().contains("Admin"))

        // The user's page also now defaults to admin
        editUserPage = manageUsersPage.table.row(0).editUser()
        assertEquals(EditLaUserPage.AccessLevelSelection.ADMIN, editUserPage.accessLevel())
    }

    @Test
    fun `a user can be deleted`() {
        // Edit Arthur Dent
        var manageUsersPage = navigator.goToManageLaUsers(1)
        var row = manageUsersPage.table.row(0)
        assertTrue(row.username().contains("Arthur Dent"))
        var editUserPage = row.editUser()

        // Delete the user
        val confirmDeletePage = editUserPage.deleteUser()
        confirmDeletePage.assertUserNameVisible("Arthur Dent")
        confirmDeletePage.assertEmailVisible("Arthur Dent") // TODO PRSD-405: fix when LA users have email addresses
        val successPage = confirmDeletePage.deleteUser()

        // The success page confirms the user is deleted
        successPage.confirmationBanner.assertHasMessage("You've removed Arthur Dent's account from Betelgeuse")
        manageUsersPage = successPage.returnToManageUsers()

        // The user is no longer in the table
        row = manageUsersPage.table.row(0)
        assertFalse(row.username().contains("Arthur Dent"))
    }
}
