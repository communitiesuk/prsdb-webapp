package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithMutableData.NestedIntegrationTestWithMutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CancelLocalCouncilUserInvitationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CancelLocalCouncilUserInvitationSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilUsersPage.Companion.ACCESS_LEVEL_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilUsersPage.Companion.ACCOUNT_STATUS_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilUsersPage.Companion.USERNAME_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class CancelLocalCouncilUserInvitationTests : IntegrationTest() {
    @Nested
    inner class LocalCouncilUserInvitation : NestedIntegrationTestWithMutableData("data-local-council-users-and-invitations.sql") {
        @Test
        fun `a LocalCouncil user invitation can be cancelled`(page: Page) {
            // Changing the pending user takes you to the cancel invitation page
            val pendingInvitationRowIndex = 2
            var manageUsersPage = navigator.goToManageLocalCouncilUsers(1)
            assertThat(manageUsersPage.table.getCell(pendingInvitationRowIndex, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(
                manageUsersPage.table.getCell(pendingInvitationRowIndex, USERNAME_COL_INDEX),
            ).containsText("invited.user@example.com")
            manageUsersPage.getChangeLink(rowIndex = pendingInvitationRowIndex).clickAndWait()
            val cancelInvitationPage = assertPageIs(page, CancelLocalCouncilUserInvitationPage::class)

            // Cancel invitation
            assertThat(cancelInvitationPage.userDetailsSection).containsText("invited.user@example.com")
            cancelInvitationPage.form.submit()
            val successPage =
                assertPageIs(
                    page,
                    CancelLocalCouncilUserInvitationSuccessPage::class,
                    mapOf("localCouncilId" to "1", "invitationId" to "1"),
                )

            // The success page confirms the user is deleted
            assertThat(
                successPage.confirmationBanner,
            ).containsText("You’ve cancelled invited.user@example.com’s invitation from BATH AND NORTH EAST SOMERSET COUNCIL")
            successPage.returnButton.clickAndWait()
            manageUsersPage = assertPageIs(page, ManageLocalCouncilUsersPage::class)

            // The invited user is no longer in the table
            assertThat(
                manageUsersPage.table.getCell(pendingInvitationRowIndex, USERNAME_COL_INDEX),
            ).not().containsText("invited.user@example.com")
        }
    }

    @Nested
    inner class LocalCouncilAdminInvitation :
        NestedIntegrationTestWithMutableData("data-local-council-invitations-user-is-system-operator.sql") {
        @Test
        fun `a LocalCouncil admin invitation can be cancelled by a system operator`(page: Page) {
            // Changing the pending user takes you to the cancel invitation page
            val pendingInvitationRowIndex = 3
            var manageUsersPage = navigator.goToManageLocalCouncilUsers(1)
            assertThat(manageUsersPage.table.getCell(pendingInvitationRowIndex, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(manageUsersPage.table.getCell(pendingInvitationRowIndex, USERNAME_COL_INDEX)).containsText("x.adminuser@example.com")
            assertThat(manageUsersPage.table.getCell(pendingInvitationRowIndex, ACCESS_LEVEL_COL_INDEX)).containsText("Admin")
            manageUsersPage.getChangeLink(rowIndex = pendingInvitationRowIndex).clickAndWait()
            val cancelInvitationPage = assertPageIs(page, CancelLocalCouncilUserInvitationPage::class)

            // Cancel invitation
            assertThat(cancelInvitationPage.userDetailsSection).containsText("x.adminuser@example.com")
            cancelInvitationPage.form.submit()
            val successPage =
                assertPageIs(
                    page,
                    CancelLocalCouncilUserInvitationSuccessPage::class,
                    mapOf("localCouncilId" to "1", "invitationId" to "4"),
                )

            // The success page confirms the user is deleted
            assertThat(
                successPage.confirmationBanner,
            ).containsText("You’ve cancelled x.adminuser@example.com’s invitation from BATH AND NORTH EAST SOMERSET COUNCIL")
            successPage.returnButton.clickAndWait()
            manageUsersPage = assertPageIs(page, ManageLocalCouncilUsersPage::class)

            // The invited user is no longer in the table
            assertThat(
                manageUsersPage.table.getCell(pendingInvitationRowIndex, USERNAME_COL_INDEX),
            ).not().containsText("x.adminuser@example.com")
        }
    }
}
