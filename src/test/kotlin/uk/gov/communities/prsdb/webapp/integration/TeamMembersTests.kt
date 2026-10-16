package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.ADMINISTRATORS_FRAGMENT
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.INVITATIONS_FRAGMENT
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@WithOrgLandlordProfile
class TeamMembersTests :
    IntegrationTestWithImmutableData(listOf("data-local.sql", "data-org-landlord-team-members.sql")) {
    @Autowired
    private lateinit var invitationRepository: OrganisationalLandlordInvitationRepository

    @Test
    fun `the administrators tab is shown by default, listing administrators alphabetically and marking the current user`() {
        // Act
        val teamMembersPage = navigator.goToTeamMembers()

        // Assert
        assertThat(teamMembersPage.heading).hasText("Team members")
        assertEquals(ADMINISTRATORS_FRAGMENT, teamMembersPage.tabs.activeTabPanelId)
        val firstAdmin = teamMembersPage.administratorsSummaryList.getRowByIndex(0)
        assertThat(firstAdmin.key).hasText("Administrator 1")
        assertThat(firstAdmin.value).hasText("Beth Admin")
        val secondAdmin = teamMembersPage.administratorsSummaryList.getRowByIndex(1)
        assertThat(secondAdmin.key).hasText("Administrator 2")
        assertThat(secondAdmin.value).hasText("Local Registrant (You)")
    }

    @Test
    fun `the editors tab lists the organisation's editors`() {
        // Arrange
        val teamMembersPage = navigator.goToTeamMembers()

        // Act
        teamMembersPage.tabs.goToEditors(editorCount = 1)

        // Assert
        val editor = teamMembersPage.editorsSummaryList.getRowByIndex(0)
        assertThat(editor.key).hasText("Editor 1")
        assertThat(editor.value).hasText("Carl Editor")
    }

    @Test
    fun `the invitations tab lists pending and expired invitations oldest first, excluding hidden invitations`() {
        // Arrange
        val teamMembersPage = navigator.goToTeamMembers()

        // Act
        teamMembersPage.tabs.goToInvitations(pendingInvitationCount = 3)

        // Assert
        assertEquals(INVITATIONS_FRAGMENT, teamMembersPage.tabs.activeTabPanelId)

        val pendingInvitations = teamMembersPage.pendingInvitationsSummaryList
        assertThat(pendingInvitations.rows).hasCount(3)
        listOf(
            "pending-org-invite-c@example.com",
            "pending-org-invite-b@example.com",
            "pending-org-invite-a@example.com",
        )
            .forEachIndexed { index, email ->
                val row = pendingInvitations.getRowByIndex(index)
                assertThat(row.key).hasText("Invitation ${index + 1}")
                assertThat(row.value).hasText(email)
                assertThat(row.actions.getActionLink("Resend")).isVisible()
                assertThat(row.actions.getActionLink("Cancel")).isVisible()
                // TODO PDJB-1758: assert the Resend link goes to the resend invitation page
                // TODO PDJB-1769: assert the Cancel link goes to the cancel invitation page
            }

        val expiredInvitations = teamMembersPage.expiredInvitationsSummaryList
        assertThat(expiredInvitations.rows).hasCount(1)
        val expiredRow = expiredInvitations.getRowByIndex(0)
        assertThat(expiredRow.key).hasText("Expired invite 1")
        assertThat(expiredRow.value).hasText("expired-org-invite-d@example.com")
        assertThat(expiredRow.actions.getActionLink("Resend")).isVisible()
        assertThat(expiredRow.actions.getActionLink("Remove")).isVisible()
        // TODO PDJB-1854: assert the Remove link goes to the remove expired invitation page

        assertThat(teamMembersPage.invitationsPanel).not().containsText("hidden-org-invite-e@example.com")
        val hiddenInvitation = invitationRepository.findByToken(UUID.fromString("1234abcd-5678-abcd-1234-567abcd2222e"))
        assertNotNull(hiddenInvitation)
        assertTrue(hiddenInvitation.isHidden)
    }
}
