package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.EditLaUserPage
import kotlin.test.assertEquals

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
        assertThat(editUserPage.email).containsText("Arthur Dent") // TODO: fix when LA users have email addresses
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

        // Reset back to basic, because the tests don't seem to be isolated from each other(!)
        // TODO: Remove once the tests are isolated from each other
        editUserPage.selectAccessLevel(EditLaUserPage.AccessLevelSelection.BASIC)
        editUserPage.submit()
    }
}
