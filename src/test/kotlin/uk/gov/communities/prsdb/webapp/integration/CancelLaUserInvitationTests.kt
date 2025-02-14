package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CancelLaUserInvitationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.CancelLaUserInvitationSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.ACCOUNT_STATUS_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.USERNAME_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

@Sql("/data-la-users-and-invitations.sql")
class CancelLaUserInvitationTests : IntegrationTest() {
    @Test
    fun `an la user invitation can be cancelled`(page: Page) {
        // Changing the pending user takes you to the cancel invitation page
        var manageUsersPage = navigator.goToManageLaUsers(1)
        assertThat(manageUsersPage.table.getCell(1, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
        assertThat(manageUsersPage.table.getCell(1, USERNAME_COL_INDEX)).containsText("invited.user@example.com")
        manageUsersPage.getChangeLink(rowIndex = 1).click()
        val cancelInvitationPage = assertPageIs(page, CancelLaUserInvitationPage::class)

        // Cancel invitation
        assertThat(cancelInvitationPage.userDetailsSection).containsText("invited.user@example.com")
        cancelInvitationPage.form.submit()
        val successPage = assertPageIs(page, CancelLaUserInvitationSuccessPage::class)

        // The success page confirms the user is deleted
        assertThat(successPage.confirmationBanner).containsText("You've cancelled invited.user@example.com's invitation from ISLE OF MAN")
        successPage.returnButton.clickAndWait()
        manageUsersPage = assertPageIs(page, ManageLaUsersPage::class)

        // The invited user is no longer in the table
        assertThat(manageUsersPage.table.getCell(1, USERNAME_COL_INDEX)).not().containsText("invited.user@example.com")
    }
}
