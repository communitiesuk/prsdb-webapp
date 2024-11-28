package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.ConfirmDeleteLaUserPage
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.DeleteLaUserSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.EditLaUserPage
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.ManageLaUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.ManageLaUsersPage.Companion.ACCESS_LEVEL_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.ManageLaUsersPage.Companion.USERNAME_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertEquals

@Sql("/data-local.sql")
class EditLAUserTests : IntegrationTest() {
    @Test
    fun `a user's access level can be updated`(page: Page) {
        // There is a basic user called Arthur Dent
        var manageUsersPage = navigator.goToManageLaUsers(1)
        assertThat(manageUsersPage.table.getCell(0, USERNAME_COL_INDEX)).containsText("Arthur Dent")
        assertThat(manageUsersPage.table.getCell(0, ACCESS_LEVEL_COL_INDEX)).containsText("Basic")

        // Editing the user shows Arthur Dent's page
        manageUsersPage.getChangeLink(rowIndex = 0).click()
        var editUserPage = assertPageIs(page, EditLaUserPage::class)
        assertThat(editUserPage.name).containsText("Arthur Dent")
        // TODO PRSD-405: fix when LA users have email addresses
        assertThat(editUserPage.email).containsText("Arthur Dent@Betelgeuse.gov.uk")
        assertEquals("false", editUserPage.isManagerRadios.getSelectedValue())

        // Update the user's access level to admin
        editUserPage.isManagerRadios.getRadio("true").click()
        editUserPage.form.submit()
        manageUsersPage = assertPageIs(page, ManageLaUsersPage::class)

        // The user is now shown as an admin in the table
        assertThat(manageUsersPage.table.getCell(0, USERNAME_COL_INDEX)).containsText("Arthur Dent")
        assertThat(manageUsersPage.table.getCell(0, ACCESS_LEVEL_COL_INDEX)).containsText("Admin")

        // The user's page also now defaults to admin
        manageUsersPage.getChangeLink(rowIndex = 0).click()
        editUserPage = assertPageIs(page, EditLaUserPage::class)
        assertEquals("true", editUserPage.isManagerRadios.getSelectedValue())
    }

    @Test
    fun `a user can be deleted`(page: Page) {
        // Edit Arthur Dent
        var manageUsersPage = navigator.goToManageLaUsers(1)
        assertThat(manageUsersPage.table.getCell(0, USERNAME_COL_INDEX)).containsText("Arthur Dent")
        manageUsersPage.getChangeLink(rowIndex = 0).click()
        val editUserPage = assertPageIs(page, EditLaUserPage::class)

        // Delete the user
        editUserPage.removeAccountButton.click()
        val confirmDeletePage = assertPageIs(page, ConfirmDeleteLaUserPage::class)
        assertThat(confirmDeletePage.userDetailsSection).containsText("Arthur Dent")
        // TODO PRSD-405: fix when LA users have email addresses
        assertThat(confirmDeletePage.userDetailsSection).containsText("Arthur Dent@Betelgeuse.gov.uk")
        confirmDeletePage.form.submit()
        val successPage = assertPageIs(page, DeleteLaUserSuccessPage::class)

        // The success page confirms the user is deleted
        assertThat(successPage.confirmationBanner).containsText("You've removed Arthur Dent's account from Betelgeuse")
        successPage.returnButton.click()
        manageUsersPage = assertPageIs(page, ManageLaUsersPage::class)

        // The user is no longer in the table
        assertThat(manageUsersPage.table.getCell(0, USERNAME_COL_INDEX)).not().containsText("Arthur Dent")
    }
}
