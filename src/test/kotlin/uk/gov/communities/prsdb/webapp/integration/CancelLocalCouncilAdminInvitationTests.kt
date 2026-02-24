package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CancelLocalCouncilAdminInvitationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CancelLocalCouncilAdminInvitationSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilAdminsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilAdminsPage.Companion.USERNAME_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class CancelLocalCouncilAdminInvitationTests : IntegrationTestWithMutableData("data-edit-local-council-admin-users-and-invitations.sql") {
    val invitationId = 1L
    val invitedEmail = "cart@example.com"

    @Test
    fun `user can be deleted`(page: Page) {
        // Navigate to the cancel la invitation page
        var manageAdminPage = navigator.goToManageLocalCouncilAdminsPage()
        assertThat(manageAdminPage.table.getCell(2, USERNAME_COL_INDEX)).containsText(invitedEmail)
        manageAdminPage.getChangeLink(2).clickAndWait()

        // Cancel invite
        val cancelAdminInvitationPage =
            assertPageIs(page, CancelLocalCouncilAdminInvitationPage::class, mapOf("invitationId" to invitationId.toString()))
        assertThat(cancelAdminInvitationPage.userDetailsSection).containsText(invitedEmail)
        cancelAdminInvitationPage.form.submit()

        // Cancel la admin invite success page
        val cancelAdminInvitationSuccessPage =
            assertPageIs(page, CancelLocalCouncilAdminInvitationSuccessPage::class, mapOf("invitationId" to invitationId.toString()))
        assertThat(
            cancelAdminInvitationSuccessPage.confirmationBanner,
        ).containsText("You’ve cancelled $invitedEmail’s invitation from BATH AND NORTH EAST SOMERSET COUNCIL")

        // Return to manage admins page
        cancelAdminInvitationSuccessPage.returnButton.clickAndWait()
        manageAdminPage = assertPageIs(page, ManageLocalCouncilAdminsPage::class)

        // Check invite is not on manage admins page
        val numberOfTableRows = manageAdminPage.table.rows.count()
        for (i in 0 until numberOfTableRows) {
            assertThat(manageAdminPage.table.getCell(i, USERNAME_COL_INDEX)).not().containsText(invitedEmail)
        }
    }
}
