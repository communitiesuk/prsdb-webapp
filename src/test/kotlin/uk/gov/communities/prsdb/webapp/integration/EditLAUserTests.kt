package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ConfirmDeleteLaUserPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteLaUserSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.EditLaUserPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.ACCESS_LEVEL_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.USERNAME_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import java.net.URI
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EditLAUserTests : IntegrationTestWithMutableData("data-local.sql") {
    @BeforeEach
    fun setupAbsoluteUrlProvider() {
        whenever(absoluteUrlProvider.buildLocalAuthorityDashboardUri()).thenReturn(URI.create("http://localhost/dashboard"))
    }

    @Test
    fun `a user's access level can be updated`(page: Page) {
        // There is a basic user called Arthur Dent
        var manageUsersPage = navigator.goToManageLaUsers(2)
        assertThat(manageUsersPage.table.getCell(0, USERNAME_COL_INDEX)).containsText("Arthur Dent")
        assertThat(manageUsersPage.table.getCell(0, ACCESS_LEVEL_COL_INDEX)).containsText("Basic")

        // Editing the user shows Arthur Dent's page
        manageUsersPage.getChangeLink(rowIndex = 0).clickAndWait()
        var editUserPage = assertPageIs(page, EditLaUserPage::class)
        assertThat(editUserPage.name).containsText("Arthur Dent")
        assertThat(editUserPage.email).containsText("Arthur.Dent@test.com")
        assertFalse(editUserPage.isManagerSelected)

        // Update the user's access level to admin
        editUserPage.selectManagerRadio()
        editUserPage.form.submit()
        manageUsersPage = assertPageIs(page, ManageLaUsersPage::class)

        // The user is now shown as an admin in the table
        assertThat(manageUsersPage.table.getCell(0, USERNAME_COL_INDEX)).containsText("Arthur Dent")
        assertThat(manageUsersPage.table.getCell(0, ACCESS_LEVEL_COL_INDEX)).containsText("Admin")

        // The user's page also now defaults to admin
        manageUsersPage.getChangeLink(rowIndex = 0).clickAndWait()
        editUserPage = assertPageIs(page, EditLaUserPage::class)
        assertTrue(editUserPage.isManagerSelected)
    }

    @Test
    fun `a user can be deleted`(page: Page) {
        // Edit Arthur Dent
        var manageUsersPage = navigator.goToManageLaUsers(2)
        assertThat(manageUsersPage.table.getCell(0, USERNAME_COL_INDEX)).containsText("Arthur Dent")
        manageUsersPage.getChangeLink(rowIndex = 0).clickAndWait()
        val editUserPage = assertPageIs(page, EditLaUserPage::class)

        // Delete the user
        editUserPage.removeAccountButton.clickAndWait()
        val confirmDeletePage = assertPageIs(page, ConfirmDeleteLaUserPage::class)
        assertThat(confirmDeletePage.userDetailsSection).containsText("Arthur Dent")
        assertThat(confirmDeletePage.userDetailsSection).containsText("Arthur.Dent@test.com")
        confirmDeletePage.form.submit()
        val successPage = assertPageIs(page, DeleteLaUserSuccessPage::class)

        // The success page confirms the user is deleted
        assertThat(
            successPage.confirmationBanner,
        ).containsText("You’ve removed Arthur Dent’s account from BATH AND NORTH EAST SOMERSET COUNCIL")
        successPage.returnButton.clickAndWait()
        manageUsersPage = assertPageIs(page, ManageLaUsersPage::class)

        // The user is no longer in the table
        assertThat(manageUsersPage.table.getCell(0, USERNAME_COL_INDEX)).not().containsText("Arthur Dent")
    }
}
