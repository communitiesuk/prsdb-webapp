package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test

class ManageLAUsersTests : IntegrationTest() {
    val localAuthorityId = 1

    @Test
    fun `table of users renders`(page: Page) {
        val managePage = navigator.goToManageLaUsers(localAuthorityId)

        val header = managePage.table.header()
        assertTrue(header.username().contains("Username"))
        assertTrue(header.accessLevel().contains("Access level"))
        assertTrue(header.accountStatus().contains("Account status"))

        val topRow = managePage.table.row(0)
        assertTrue(topRow.username().contains("Arthur Dent"))
        assertTrue(topRow.accessLevel().contains("Basic"))
        assertTrue(topRow.accountStatus().contains("ACTIVE"))

        val nextRow = managePage.table.row(1)
        assertTrue(nextRow.accessLevel().contains("Admin"))
    }

    @Test
    fun `invite button goes to invite new user page`() {
        val managePage = navigator.goToManageLaUsers(localAuthorityId)
        managePage.inviteNewUser()
    }

    @Test
    fun `return to dashboard button is visible`() {
        val managePage = navigator.goToManageLaUsers(localAuthorityId)
        assertThat(managePage.returnToDashboardButton).isVisible()
    }

    @Test
    fun `pagination component renders with more than 10 table entries`(page: Page) {
        var managePage = navigator.goToManageLaUsers(localAuthorityId)
        managePage.pagination.assertNextIsVisible()
        managePage.pagination.assertPageNumberIsCurrent(1)
        managePage.pagination.assertPageNumberIsVisible(2)

        managePage = managePage.pagination.clickLink(2)

        managePage.pagination.assertPreviousIsVisible()
        managePage.pagination.assertPageNumberIsVisible(1)
        managePage.pagination.assertPageNumberIsCurrent(2)
    }
}
