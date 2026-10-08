package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.TeamMembersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteTeamMemberJourneyPages.InviteTeamMemberFormPage
import kotlin.test.assertEquals

@WithOrgLandlordProfile
class InviteTeamMemberJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var organisationalLandlordInvitationRepository: OrganisationalLandlordInvitationRepository

    @Test
    fun `an admin can invite a team member and sees a success banner, which clears on refresh`(page: Page) {
        // 1. Team members page
        var teamMembersPage = navigator.goToTeamMembers()
        assertThat(teamMembersPage.successBanner).isHidden()
        teamMembersPage.inviteTeamMemberButton.clickAndWait()

        // 2. Invite a team member page
        val invitePage = assertPageIs(page, InviteTeamMemberFormPage::class)
        invitePage.submitInvitation("new.member@example.com", OrganisationalLandlordUserRole.EDITOR)

        // 3. Back on the team members page, with the invitation stored and a success banner shown
        teamMembersPage = assertPageIs(page, TeamMembersPage::class)
        assertThat(teamMembersPage.successBanner.title).containsText("Success")
        assertThat(teamMembersPage.successBanner.content.heading).containsText("Invitation sent")
        val invitation =
            organisationalLandlordInvitationRepository.findAll().single { it.invitedEmail == "new.member@example.com" }
        assertEquals(OrganisationalLandlordUserRole.EDITOR, invitation.role)
        assertEquals("Local Organisation Landlord", invitation.organisationalLandlord.name)

        // 4. The banner is not shown again on refresh
        page.reload()
        assertThat(teamMembersPage.successBanner).isHidden()
    }
}
