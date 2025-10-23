package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteLaAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.EditLaAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaAdminsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class EditLaAdminSinglePageTests : IntegrationTestWithImmutableData("data-edit-la-admin-users-and-invitations.sql") {
    val laAdminId = 1L

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
    }

    @Nested
    inner class DeleteAdmin {
        @Test
        fun `back link goes to edit admin page`(page: Page) {
            val deleteAdminPage = navigator.goToDeleteLaAdminPage(laAdminId)
            deleteAdminPage.backLink.clickAndWait()
            assertPageIs(page, EditLaAdminPage::class, mapOf("laAdminId" to laAdminId.toString()))
        }
    }
}
