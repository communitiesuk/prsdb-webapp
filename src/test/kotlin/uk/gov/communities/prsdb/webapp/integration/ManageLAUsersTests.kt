package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLaUserPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.ACCESS_LEVEL_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.ACCOUNT_STATUS_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.ACTIONS_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaUsersPage.Companion.USERNAME_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.Test
import kotlin.test.assertEquals

@Sql("/data-local.sql")
class ManageLAUsersTests : IntegrationTest() {
    val localAuthorityId = 1

    @Test
    fun `table of users renders`() {
        val managePage = navigator.goToManageLaUsers(localAuthorityId)

        // Header
        assertThat(managePage.table.getHeaderCell(USERNAME_COL_INDEX)).containsText("Username")
        assertThat(managePage.table.getHeaderCell(ACCESS_LEVEL_COL_INDEX)).containsText("Access level")
        assertThat(managePage.table.getHeaderCell(ACCOUNT_STATUS_COL_INDEX)).containsText("Account status")

        // Arthur Dent Row
        assertThat(managePage.table.getCell(0, USERNAME_COL_INDEX)).containsText("Arthur Dent")
        assertThat(managePage.table.getCell(0, ACCESS_LEVEL_COL_INDEX)).containsText("Basic")
        assertThat(managePage.table.getCell(0, ACCOUNT_STATUS_COL_INDEX)).containsText("ACTIVE")

        // Admin Row
        assertThat(managePage.table.getCell(1, ACCESS_LEVEL_COL_INDEX)).containsText("Admin")

        // Current User Row
        assertThat(managePage.table.getCell(4, ACTIONS_COL_INDEX)).isEmpty()
    }

    @Test
    fun `invite button goes to invite new user page`(page: Page) {
        val managePage = navigator.goToManageLaUsers(localAuthorityId)
        managePage.inviteAnotherUserButton.click()
        assertPageIs(page, InviteNewLaUserPage::class)
    }

    @Test
    fun `return to dashboard button is visible`() {
        val managePage = navigator.goToManageLaUsers(localAuthorityId)
        assertThat(managePage.returnToDashboardButton).isVisible()
    }

    @Test
    fun `pagination component renders with more than 10 table entries`(page: Page) {
        var managePage = navigator.goToManageLaUsers(localAuthorityId)
        val pagination = managePage.getPaginationComponent()
        assertThat(pagination.getNextLink()).isVisible()
        assertEquals("1", pagination.getCurrentPageNumberLinkText())
        assertThat(pagination.getPageNumberLink(2)).isVisible()

        pagination.getPageNumberLink(2).click()
        managePage = assertPageIs(page, ManageLaUsersPage::class)

        assertThat(pagination.getPreviousLink()).isVisible()
        assertThat(pagination.getPageNumberLink(1)).isVisible()
        assertEquals("2", pagination.getCurrentPageNumberLinkText())
    }
}
