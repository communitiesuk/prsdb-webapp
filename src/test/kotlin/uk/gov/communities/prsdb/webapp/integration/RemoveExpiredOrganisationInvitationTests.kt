package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.INVITATIONS_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.TeamMembersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.assertEquals

@WithOrgLandlordProfile
class RemoveExpiredOrganisationInvitationTests :
    IntegrationTestWithMutableData(listOf("data-local.sql", "data-org-landlord-team-members.sql")) {
    @Test
    fun `removing an expired invitation hides it and returns to the invitations tab`(page: Page) {
        // Arrange
        val teamMembersPage = navigator.goToTeamMembers()
        teamMembersPage.tabs.goToInvitations(pendingInvitationCount = 3)
        assertThat(teamMembersPage.expiredInvitationsSummaryList.rows).hasCount(2)

        // Act
        teamMembersPage.expiredInvitationsSummaryList
            .getRowByIndex(0)
            .clickNamedActionLinkAndWait("Remove")

        // Assert
        val reloadedPage = assertPageIs(page, TeamMembersPage::class)
        assertEquals(INVITATIONS_FRAGMENT, reloadedPage.tabs.activeTabPanelId)
        assertThat(reloadedPage.expiredInvitationsSummaryList.rows).hasCount(1)
        val remainingRow = reloadedPage.expiredInvitationsSummaryList.getRowByIndex(0)
        assertThat(remainingRow.key).hasText("Expired invite 1")
        assertThat(remainingRow.value).hasText("expired-org-invite-d@example.com")
        assertThat(reloadedPage.invitationsPanel).not().containsText("expired-org-invite-f@example.com")
    }

    @Test
    fun `a removed expired invitation stays hidden after reloading the page`(page: Page) {
        // Arrange
        val teamMembersPage = navigator.goToTeamMembers()
        teamMembersPage.tabs.goToInvitations(pendingInvitationCount = 3)
        teamMembersPage.expiredInvitationsSummaryList
            .getRowByIndex(0)
            .clickNamedActionLinkAndWait("Remove")
        assertPageIs(page, TeamMembersPage::class)

        // Act
        val reloadedPage = navigator.goToTeamMembers()
        reloadedPage.tabs.goToInvitations(pendingInvitationCount = 3)

        // Assert
        assertThat(reloadedPage.expiredInvitationsSummaryList.rows).hasCount(1)
        assertThat(reloadedPage.invitationsPanel).not().containsText("expired-org-invite-f@example.com")
    }
}
