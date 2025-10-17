package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithImmutableData.NestedIntegrationTestWithImmutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteLaAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaAdminsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaAdminsPage.Companion.ACCOUNT_STATUS_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaAdminsPage.Companion.LOCAL_COUNCIL_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLaAdminsPage.Companion.USERNAME_COL_INDEX
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.Test
import kotlin.test.assertEquals

class ManageLAAdminsTests : IntegrationTest() {
    @Nested
    inner class WithAdminUsersAndInvites : NestedIntegrationTestWithImmutableData(
        "data-la-admin-users-and-invitations-user-is-system-operator.sql",
    ) {
        @Test
        fun `invite another admin button goes to invite new user page`(page: Page) {
            val manageAdminPage = navigator.goToManageLaAdminsPage()
            manageAdminPage.inviteAnotherAdminButton.clickAndWait()
            assertPageIs(page, InviteLaAdminPage::class)
        }

        @Test
        fun `pagination component renders with more than 10 table entries`(page: Page) {
            val manageAdminPage = navigator.goToManageLaAdminsPage()
            val pagination = manageAdminPage.getPaginationComponent()
            assertEquals(manageAdminPage.table.rows.count(), 10)
            assertThat(pagination.nextLink).isVisible()
            assertEquals("1", pagination.currentPageNumberLinkText)
            assertThat(pagination.getPageNumberLink(2)).isVisible()

            pagination.getPageNumberLink(2).clickAndWait()
            assertPageIs(page, ManageLaAdminsPage::class)

            assertThat(pagination.previousLink).isVisible()
            assertThat(pagination.getPageNumberLink(1)).isVisible()
            assertEquals("2", pagination.currentPageNumberLinkText)
        }

        @Test
        fun `admins are listed in the correct order`(page: Page) {
            val manageAdminPage = navigator.goToManageLaAdminsPage()

            // Header
            assertThat(manageAdminPage.table.headerRow.getCell(USERNAME_COL_INDEX)).containsText("Username")
            assertThat(manageAdminPage.table.headerRow.getCell(LOCAL_COUNCIL_COL_INDEX)).containsText("Local council")
            assertThat(manageAdminPage.table.headerRow.getCell(ACCOUNT_STATUS_COL_INDEX)).containsText("Account status")

            // Active rows before pending rows
            assertThat(manageAdminPage.table.getCell(0, ACCOUNT_STATUS_COL_INDEX)).containsText("ACTIVE")
            assertThat(manageAdminPage.table.getCell(1, ACCOUNT_STATUS_COL_INDEX)).containsText("ACTIVE")
            assertThat(manageAdminPage.table.getCell(2, ACCOUNT_STATUS_COL_INDEX)).containsText("ACTIVE")
            assertThat(manageAdminPage.table.getCell(3, ACCOUNT_STATUS_COL_INDEX)).containsText("ACTIVE")
            assertThat(manageAdminPage.table.getCell(4, ACCOUNT_STATUS_COL_INDEX)).containsText("ACTIVE")
            assertThat(manageAdminPage.table.getCell(5, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(manageAdminPage.table.getCell(6, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(manageAdminPage.table.getCell(7, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(manageAdminPage.table.getCell(8, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")
            assertThat(manageAdminPage.table.getCell(9, ACCOUNT_STATUS_COL_INDEX)).containsText("PENDING")

            // Local councils in alphabetical order within status

            // ACTIVE
            assertThat(manageAdminPage.table.getCell(0, LOCAL_COUNCIL_COL_INDEX)).containsText("BATH AND NORTH EAST SOMERSET COUNCIL")
            assertThat(manageAdminPage.table.getCell(1, LOCAL_COUNCIL_COL_INDEX)).containsText("BATH AND NORTH EAST SOMERSET COUNCIL")
            assertThat(manageAdminPage.table.getCell(2, LOCAL_COUNCIL_COL_INDEX)).containsText("NORTH SOMERSET COUNCIL")
            assertThat(manageAdminPage.table.getCell(3, LOCAL_COUNCIL_COL_INDEX)).containsText("NORTH SOMERSET COUNCIL")
            assertThat(manageAdminPage.table.getCell(4, LOCAL_COUNCIL_COL_INDEX)).containsText("NORTH SOMERSET COUNCIL")

            // PENDING
            assertThat(manageAdminPage.table.getCell(5, LOCAL_COUNCIL_COL_INDEX)).containsText("BATH AND NORTH EAST SOMERSET COUNCIL")
            assertThat(manageAdminPage.table.getCell(6, LOCAL_COUNCIL_COL_INDEX)).containsText("BATH AND NORTH EAST SOMERSET COUNCIL")
            assertThat(manageAdminPage.table.getCell(7, LOCAL_COUNCIL_COL_INDEX)).containsText("NORTH SOMERSET COUNCIL")
            assertThat(manageAdminPage.table.getCell(8, LOCAL_COUNCIL_COL_INDEX)).containsText("NORTH SOMERSET COUNCIL")
            assertThat(manageAdminPage.table.getCell(9, LOCAL_COUNCIL_COL_INDEX)).containsText("NORTH SOMERSET COUNCIL")

            // Names in alphabetical order within councils

            // ACTIVE - BATH AND NORTH EAST SOMERSET COUNCIL
            assertThat(manageAdminPage.table.getCell(0, USERNAME_COL_INDEX)).containsText("C name")
            assertThat(manageAdminPage.table.getCell(1, USERNAME_COL_INDEX)).containsText("E name")

            // ACTIVE - NORTH SOMERSET COUNCIL
            assertThat(manageAdminPage.table.getCell(2, USERNAME_COL_INDEX)).containsText("A name")
            assertThat(manageAdminPage.table.getCell(3, USERNAME_COL_INDEX)).containsText("B name")
            assertThat(manageAdminPage.table.getCell(4, USERNAME_COL_INDEX)).containsText("D name")

            // PENDING - BATH AND NORTH EAST SOMERSET COUNCIL
            assertThat(manageAdminPage.table.getCell(5, USERNAME_COL_INDEX)).containsText("G@example.com")
            assertThat(manageAdminPage.table.getCell(6, USERNAME_COL_INDEX)).containsText("J@example.com")

            // PENDING - NORTH SOMERSET COUNCIL
            assertThat(manageAdminPage.table.getCell(7, USERNAME_COL_INDEX)).containsText("F@example.com")
            assertThat(manageAdminPage.table.getCell(8, USERNAME_COL_INDEX)).containsText("H@example.com")
            assertThat(manageAdminPage.table.getCell(9, USERNAME_COL_INDEX)).containsText("I@example.com")
        }
    }
}
