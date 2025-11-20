package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.DeleteLocalCouncilAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.EditLocalCouncilAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilAdminsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class EditLocalCouncilAdminSinglePageTests : IntegrationTestWithImmutableData("data-edit-local-council-admin-users-and-invitations.sql") {
    val localCouncilAdminId = 1L

    @Nested
    inner class EditAdmin {
        @Test
        fun `back link goes to manage admin users page`(page: Page) {
            val editAdminPage = navigator.goToEditAdminsPage(localCouncilAdminId)
            editAdminPage.backLink.clickAndWait()
            assertPageIs(page, ManageLocalCouncilAdminsPage::class)
        }

        @Test
        fun `remove this account button goes to delete admin page`(page: Page) {
            val editAdminPage = navigator.goToEditAdminsPage(localCouncilAdminId)
            editAdminPage.removeAccountButton.clickAndWait()
            assertPageIs(page, DeleteLocalCouncilAdminPage::class, mapOf("localCouncilAdminId" to localCouncilAdminId.toString()))
        }
    }

    @Nested
    inner class DeleteAdmin {
        @Test
        fun `back link goes to edit admin page`(page: Page) {
            val deleteAdminPage = navigator.goToDeleteLocalCouncilAdminPage(localCouncilAdminId)
            deleteAdminPage.backLink.clickAndWait()
            assertPageIs(page, EditLocalCouncilAdminPage::class, mapOf("localCouncilAdminId" to localCouncilAdminId.toString()))
        }
    }
}
