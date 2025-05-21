package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLaUserPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.ACCESS_LEVEL_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.ACCOUNT_STATUS_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.ACTIONS_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.USERNAME_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.Test
import kotlin.test.assertEquals

class ManageLAUsersTests : SinglePageTestWithSeedData("data-local.sql") {
    val localAuthorityId = 1

    @Test
    fun `invite button goes to invite new user page`(page: Page) {
        val managePage = navigator.goToManageLaUsers(localAuthorityId)
        managePage.inviteAnotherUserButton.clickAndWait()
        assertPageIs(page, InviteNewLaUserPage::class)
    }

    @Test
    fun `pagination component renders with more than 10 table entries`(page: Page) {
        var managePage = navigator.goToManageLaUsers(localAuthorityId)
        val pagination = managePage.getPaginationComponent()
        assertThat(pagination.nextLink).isVisible()
        assertEquals("1", pagination.currentPageNumberLinkText)
        assertThat(pagination.getPageNumberLink(2)).isVisible()

        pagination.getPageNumberLink(2).clickAndWait()
        managePage = assertPageIs(page, ManageLaUsersPage::class)

        assertThat(pagination.previousLink).isVisible()
        assertThat(pagination.getPageNumberLink(1)).isVisible()
        assertEquals("2", pagination.currentPageNumberLinkText)
    }

    @Nested
    inner class UserIsLaAdminButNotSystemOperator : NestedSinglePageTestWithSeedData("data-la-users-and-invitations.sql") {
        @Test
        fun `table of users renders`() {
            val managePage = navigator.goToManageLaUsers(localAuthorityId)

            // Header
            assertThat(managePage.table.headerRow.getCell(USERNAME_COL_INDEX)).containsText("Username")
            assertThat(managePage.table.headerRow.getCell(ACCESS_LEVEL_COL_INDEX)).containsText("Access level")
            assertThat(managePage.table.headerRow.getCell(ACCOUNT_STATUS_COL_INDEX)).containsText("Account status")

            // Arthur Dent Row
            assertThat(managePage.table.getCell(0, USERNAME_COL_INDEX)).containsText("Arthur Dent")
            assertThat(managePage.table.getCell(0, ACCESS_LEVEL_COL_INDEX)).containsText("Basic")
            assertThat(managePage.table.getCell(0, ACCOUNT_STATUS_COL_INDEX)).containsText("ACTIVE")

            // Admin Row
            assertThat(managePage.table.getCell(1, ACCESS_LEVEL_COL_INDEX)).containsText("Admin")

            // Current User Row
            assertThat(managePage.table.getCell(1, ACTIONS_COL_INDEX)).isEmpty()

            // Rows are Arthur Dent (la user) row, Admin row, and 2 non-admin invite row and that is all - no admin invite
            assertEquals(4, managePage.table.rows.count())
            assertThat(managePage.table.getCell(2, ACCESS_LEVEL_COL_INDEX)).containsText("Basic")
            assertThat(managePage.table.getCell(2, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(managePage.table.getCell(3, ACCESS_LEVEL_COL_INDEX)).containsText("Basic")
            assertThat(managePage.table.getCell(3, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
        }

        @Test
        fun `return to dashboard button goes to LA dashboard`(page: Page) {
            val managePage = navigator.goToManageLaUsers(localAuthorityId)
            managePage.returnToDashboardButton.clickAndWait()
            assertPageIs(page, LocalAuthorityDashboardPage::class)
        }
    }

    @Nested
    inner class UserIsSystemOperatorButNotLaAdmin : NestedSinglePageTestWithSeedData("data-la-invitations-user-is-system-operator.sql") {
        @Test
        fun `table renders all user types including la admin invitations`() {
            val managePage = navigator.goToManageLaUsers(localAuthorityId)

            // Header
            assertThat(managePage.table.headerRow.getCell(USERNAME_COL_INDEX)).containsText("Username")
            assertThat(managePage.table.headerRow.getCell(ACCESS_LEVEL_COL_INDEX)).containsText("Access level")
            assertThat(managePage.table.headerRow.getCell(ACCOUNT_STATUS_COL_INDEX)).containsText("Account status")

            // Arthur Dent Row
            assertThat(managePage.table.getCell(0, USERNAME_COL_INDEX)).containsText("Arthur Dent")
            assertThat(managePage.table.getCell(0, ACCESS_LEVEL_COL_INDEX)).containsText("Basic")
            assertThat(managePage.table.getCell(0, ACCOUNT_STATUS_COL_INDEX)).containsText("ACTIVE")

            // Admin user Row
            assertThat(managePage.table.getCell(1, ACCOUNT_STATUS_COL_INDEX)).containsText("ACTIVE")
            assertThat(managePage.table.getCell(1, ACCESS_LEVEL_COL_INDEX)).containsText("Admin")

            // Non-admin invitation rows
            assertThat(managePage.table.getCell(2, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(managePage.table.getCell(2, ACCESS_LEVEL_COL_INDEX)).containsText("Basic")
            assertThat(managePage.table.getCell(5, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(managePage.table.getCell(5, ACCESS_LEVEL_COL_INDEX)).containsText("Basic")

            // Admin invitation rows
            assertThat(managePage.table.getCell(3, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(managePage.table.getCell(3, ACCESS_LEVEL_COL_INDEX)).containsText("Admin")
            assertThat(managePage.table.getCell(4, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(managePage.table.getCell(4, ACCESS_LEVEL_COL_INDEX)).containsText("Admin")

            assertEquals(6, managePage.table.rows.count())
        }

        // TODO: PRSD-672 - add tests for Return To Dashboard button going to System Operator dashboard
    }
}
