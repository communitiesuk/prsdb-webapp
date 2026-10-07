package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat

@WithOrgLandlordProfile
class InviteTeamMemberSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `the invite page names the admin's organisation`() {
        // Act
        val invitePage = navigator.goToInviteTeamMemberPage()

        // Assert
        assertThat(invitePage.heading).hasText("Invite a team member")
        assertThat(invitePage.introduction).containsText("Local Organisation Landlord")
    }
}
