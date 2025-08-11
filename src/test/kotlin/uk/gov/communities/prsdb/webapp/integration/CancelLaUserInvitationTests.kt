package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithMutableData.NestedIntegrationTestWithMutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CancelLaUserInvitationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CancelLaUserInvitationSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.ACCESS_LEVEL_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.ACCOUNT_STATUS_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.USERNAME_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class CancelLaUserInvitationTests : IntegrationTest() {
    @Nested
    inner class LaUserInvitation : NestedIntegrationTestWithMutableData("data-la-users-and-invitations.sql") {
        @Test
        fun `an la user invitation can be cancelled`(page: Page) {
            // Changing the pending user takes you to the cancel invitation page
            val pendingInvitationRowIndex = 2
            var manageUsersPage = navigator.goToManageLaUsers(2)
            assertThat(manageUsersPage.table.getCell(pendingInvitationRowIndex, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(
                manageUsersPage.table.getCell(pendingInvitationRowIndex, USERNAME_COL_INDEX),
            ).containsText("invited.user@example.com")
            manageUsersPage.getChangeLink(rowIndex = pendingInvitationRowIndex).clickAndWait()
            val cancelInvitationPage = assertPageIs(page, CancelLaUserInvitationPage::class)

            // Cancel invitation
            assertThat(cancelInvitationPage.userDetailsSection).containsText("invited.user@example.com")
            cancelInvitationPage.form.submit()
            val successPage = assertPageIs(page, CancelLaUserInvitationSuccessPage::class)

            // The success page confirms the user is deleted
            assertThat(
                successPage.confirmationBanner,
            ).containsText("You've cancelled invited.user@example.com's invitation from BATH AND NORTH EAST SOMERSET COUNCIL")
            successPage.returnButton.clickAndWait()
            manageUsersPage = assertPageIs(page, ManageLaUsersPage::class)

            // The invited user is no longer in the table
            assertThat(
                manageUsersPage.table.getCell(pendingInvitationRowIndex, USERNAME_COL_INDEX),
            ).not().containsText("invited.user@example.com")
        }
    }

    @Nested
    inner class LaAdminInvitation : NestedIntegrationTestWithMutableData("data-la-invitations-user-is-system-operator.sql") {
        @Test
        fun `an la admin invitation can be cancelled by a system operator`(page: Page) {
            // Changing the pending user takes you to the cancel invitation page
            val pendingInvitationRowIndex = 3
            var manageUsersPage = navigator.goToManageLaUsers(2)
            assertThat(manageUsersPage.table.getCell(pendingInvitationRowIndex, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(manageUsersPage.table.getCell(pendingInvitationRowIndex, USERNAME_COL_INDEX)).containsText("x.adminuser@example.com")
            assertThat(manageUsersPage.table.getCell(pendingInvitationRowIndex, ACCESS_LEVEL_COL_INDEX)).containsText("Admin")
            manageUsersPage.getChangeLink(rowIndex = pendingInvitationRowIndex).clickAndWait()
            val cancelInvitationPage = assertPageIs(page, CancelLaUserInvitationPage::class)

            // Cancel invitation
            assertThat(cancelInvitationPage.userDetailsSection).containsText("x.adminuser@example.com")
            cancelInvitationPage.form.submit()
            val successPage = assertPageIs(page, CancelLaUserInvitationSuccessPage::class)

            // The success page confirms the user is deleted
            assertThat(
                successPage.confirmationBanner,
            ).containsText("You've cancelled x.adminuser@example.com's invitation from BATH AND NORTH EAST SOMERSET COUNCIL")
            successPage.returnButton.clickAndWait()
            manageUsersPage = assertPageIs(page, ManageLaUsersPage::class)

            // The invited user is no longer in the table
            assertThat(
                manageUsersPage.table.getCell(pendingInvitationRowIndex, USERNAME_COL_INDEX),
            ).not().containsText("x.adminuser@example.com")
        }
    }
}
