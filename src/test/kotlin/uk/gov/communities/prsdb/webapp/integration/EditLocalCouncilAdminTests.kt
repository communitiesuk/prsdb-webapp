package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteLocalCouncilAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteLocalCouncilAdminSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.EditLocalCouncilAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilAdminsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilAdminsPage.Companion.USERNAME_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import java.net.URI
import kotlin.test.assertTrue

class EditLocalCouncilAdminTests : IntegrationTestWithMutableData("data-edit-local-council-admin-users-and-invitations.sql") {
    val localCouncilAdminId = 1L
    val localCouncilAdminName = "Art Name"
    val localCouncilAdminEmail = "art@example.com"

    @Nested
    inner class EditAdmin {
        @Test
        fun `an admin access can be changed to basic`(page: Page) {
            // Click the change link for the la admin to edit
            var manageAdminPage = navigator.goToManageLocalCouncilAdminsPage()
            assertThat(manageAdminPage.table.getCell(0, USERNAME_COL_INDEX)).containsText(localCouncilAdminName)
            manageAdminPage.getChangeLink(0).clickAndWait()

            // Demote the admin to basic user
            val editAdminPage =
                assertPageIs(
                    page,
                    EditLocalCouncilAdminPage::class,
                    mapOf("localCouncilAdminId" to localCouncilAdminId.toString()),
                )
            assertThat(editAdminPage.name).containsText(localCouncilAdminName)
            assertThat(editAdminPage.email).containsText(localCouncilAdminEmail)
            assertTrue(editAdminPage.isManagerSelected)

            // Update the user's access level to basic
            editAdminPage.selectNotManagerRadio()
            editAdminPage.form.submit()
            manageAdminPage = assertPageIs(page, ManageLocalCouncilAdminsPage::class)

            // Check user is not on manage admins page
            val numberOfTableRows = manageAdminPage.table.rows.count()
            for (i in 0 until numberOfTableRows) {
                assertThat(manageAdminPage.table.getCell(i, USERNAME_COL_INDEX)).not().containsText(localCouncilAdminName)
            }
        }
    }

    @Nested
    inner class DeleteAdmin {
        @Test
        fun `user can be deleted`(page: Page) {
            whenever(absoluteUrlProvider.buildLocalCouncilDashboardUri()).thenReturn(URI.create("http://localhost/dashboard"))

            // Navigate to the delete page for la admin
            var manageAdminPage = navigator.goToManageLocalCouncilAdminsPage()
            assertThat(manageAdminPage.table.getCell(0, USERNAME_COL_INDEX)).containsText(localCouncilAdminName)
            manageAdminPage.getChangeLink(0).clickAndWait()
            val editAdminPage =
                assertPageIs(
                    page,
                    EditLocalCouncilAdminPage::class,
                    mapOf("localCouncilAdminId" to localCouncilAdminId.toString()),
                )
            editAdminPage.removeAccountButton.clickAndWait()

            // Delete la admin
            val deleteAdminPage =
                assertPageIs(
                    page,
                    DeleteLocalCouncilAdminPage::class,
                    mapOf("localCouncilAdminId" to localCouncilAdminId.toString()),
                )
            assertThat(deleteAdminPage.userDetailsSection).containsText(localCouncilAdminName)
            assertThat(deleteAdminPage.userDetailsSection).containsText(localCouncilAdminEmail)
            deleteAdminPage.form.submit()

            // Delete la admin success page
            val deleteAdminSuccessPage =
                assertPageIs(
                    page,
                    DeleteLocalCouncilAdminSuccessPage::class,
                    mapOf("localCouncilAdminId" to localCouncilAdminId.toString()),
                )
            assertThat(
                deleteAdminSuccessPage.confirmationBanner,
            ).containsText("You’ve removed $localCouncilAdminName’s account from BATH AND NORTH EAST SOMERSET COUNCIL")

            // Return to manage admins page
            deleteAdminSuccessPage.returnButton.clickAndWait()
            manageAdminPage = assertPageIs(page, ManageLocalCouncilAdminsPage::class)

            // Check user is not on manage admins page
            val numberOfTableRows = manageAdminPage.table.rows.count()
            for (i in 0 until numberOfTableRows) {
                assertThat(manageAdminPage.table.getCell(i, USERNAME_COL_INDEX)).not().containsText(localCouncilAdminName)
            }
        }
    }
}
