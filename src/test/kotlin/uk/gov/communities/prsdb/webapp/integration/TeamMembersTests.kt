package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import kotlin.test.assertEquals

@WithOrgLandlordProfile
class TeamMembersTests : IntegrationTestWithImmutableData(listOf("data-local.sql", "data-org-landlord-team-members.sql")) {
    @Test
    fun `the team members page lists administrators and editors alphabetically with the current user's row not available`(page: Page) {
        val teamMembersPage = navigator.goToTeamMembers()

        assertThat(teamMembersPage.heading).hasText("Team members")
        assertThat(teamMembersPage.inviteTeamMemberButton).isVisible()
        assertEquals("administrators", teamMembersPage.tabs.activeTabPanelId)

        val firstAdmin = teamMembersPage.administratorsSummaryList.getRowByIndex(0)
        assertThat(firstAdmin.key).hasText("Administrator 1")
        assertThat(firstAdmin.value).hasText("Beth Admin")
        assertThat(firstAdmin.actions.firstActionLink).hasText("Change Administrator 1")
        val secondAdmin = teamMembersPage.administratorsSummaryList.getRowByIndex(1)
        assertThat(secondAdmin.key).hasText("Administrator 2")
        assertThat(secondAdmin.value).hasText("Local Registrant (You)")
        assertThat(secondAdmin.actions).hasText("Not available")

        teamMembersPage.tabs.goToEditors(editorCount = 1)
        val editor = teamMembersPage.editorsSummaryList.getRowByIndex(0)
        assertThat(editor.key).hasText("Editor 1")
        assertThat(editor.value).hasText("Carl Editor")
        assertThat(editor.actions.firstActionLink).hasText("Change Editor 1")
    }
}
