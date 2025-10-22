package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteLaAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteLaAdminSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.EditLaAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaAdminsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaAdminsPage.Companion.USERNAME_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertTrue

class EditLaAdminTests : IntegrationTestWithMutableData("data-edit-la-admin-users-and-invitations.sql") {
    val laAdminId = 1L
    val laAdminName = "Art Name"
    val laAdminEmail = "art@example.com"

    @Nested
    inner class EditAdmin {
        @Test
        fun `back link goes to manage admin users page`(page: Page) {
            val editAdminPage = navigator.goToEditAdminsPage(laAdminId)
            editAdminPage.backLink.clickAndWait()
            assertPageIs(page, ManageLaAdminsPage::class)
        }

        @Test
        fun `remove this account button goes to delete admin page`(page: Page) {
            val editAdminPage = navigator.goToEditAdminsPage(laAdminId)
            editAdminPage.removeAccountButton.clickAndWait()
            assertPageIs(page, DeleteLaAdminPage::class, mapOf("laAdminId" to laAdminId.toString()))
        }

        @Test
        fun `an admin access can be changed to basic`(page: Page) {
            // Click the change link for the la admin to edit
            var manageAdminPage = navigator.goToManageLaAdminsPage()
            assertThat(manageAdminPage.table.getCell(0, USERNAME_COL_INDEX)).containsText(laAdminName)
            manageAdminPage.getChangeLink(0).clickAndWait()

            // Demote the admin to basic user
            val editAdminPage = assertPageIs(page, EditLaAdminPage::class, mapOf("laAdminId" to laAdminId.toString()))
            assertThat(editAdminPage.name).containsText(laAdminName)
            assertThat(editAdminPage.email).containsText(laAdminEmail)
            assertTrue(editAdminPage.isManagerSelected)

            // Update the user's access level to basic
            editAdminPage.selectNotManagerRadio()
            editAdminPage.form.submit()
            manageAdminPage = assertPageIs(page, ManageLaAdminsPage::class)

            // Check user is not on manage admins page
            val numberOfTableRows = manageAdminPage.table.rows.count()
            for (i in 0 until numberOfTableRows) {
                assertThat(manageAdminPage.table.getCell(i, USERNAME_COL_INDEX)).not().containsText(laAdminName)
            }
        }
    }

    @Nested
    inner class DeleteAdmin {
        @Test
        fun `back link goes to edit admin page`(page: Page) {
            val deleteAdminPage = navigator.goToDeleteLaAdminPage(laAdminId)
            deleteAdminPage.backLink.clickAndWait()
            assertPageIs(page, EditLaAdminPage::class, mapOf("laAdminId" to laAdminId.toString()))
        }

        @Test
        fun `user can be deleted`(page: Page) {
            // Navigate to the delete page for la admin
            var manageAdminPage = navigator.goToManageLaAdminsPage()
            assertThat(manageAdminPage.table.getCell(0, USERNAME_COL_INDEX)).containsText(laAdminName)
            manageAdminPage.getChangeLink(0).clickAndWait()
            val editAdminPage = assertPageIs(page, EditLaAdminPage::class, mapOf("laAdminId" to laAdminId.toString()))
            editAdminPage.removeAccountButton.clickAndWait()

            // Delete la admin
            val deleteAdminPage = assertPageIs(page, DeleteLaAdminPage::class, mapOf("laAdminId" to laAdminId.toString()))
            assertThat(deleteAdminPage.userDetailsSection).containsText(laAdminName)
            assertThat(deleteAdminPage.userDetailsSection).containsText(laAdminEmail)
            deleteAdminPage.form.submit()

            // Delete la admin success page
            val deleteAdminSuccessPage = assertPageIs(page, DeleteLaAdminSuccessPage::class, mapOf("laAdminId" to laAdminId.toString()))
            assertThat(
                deleteAdminSuccessPage.confirmationBanner,
            ).containsText("You’ve removed $laAdminName’s account from BATH AND NORTH EAST SOMERSET COUNCIL")

            // Return to manage admins page
            deleteAdminSuccessPage.returnButton.clickAndWait()
            manageAdminPage = assertPageIs(page, ManageLaAdminsPage::class)

            // Check user is not on manage admins page
            val numberOfTableRows = manageAdminPage.table.rows.count()
            for (i in 0 until numberOfTableRows) {
                assertThat(manageAdminPage.table.getCell(i, USERNAME_COL_INDEX)).not().containsText(laAdminName)
            }
        }
    }
}
