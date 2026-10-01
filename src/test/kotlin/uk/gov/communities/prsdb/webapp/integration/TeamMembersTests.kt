package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.ADMINISTRATORS_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import kotlin.test.assertEquals

@WithOrgLandlordProfile
class TeamMembersTests : IntegrationTestWithImmutableData(listOf("data-local.sql", "data-org-landlord-team-members.sql")) {
    @Test
    fun `the team members page lists administrators and editors alphabetically and marks the current user`() {
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

        // Act
        teamMembersPage.tabs.goToEditors(editorCount = 1)

        // Assert
        val editor = teamMembersPage.editorsSummaryList.getRowByIndex(0)
        assertThat(editor.key).hasText("Editor 1")
        assertThat(editor.value).hasText("Carl Editor")
    }
}
