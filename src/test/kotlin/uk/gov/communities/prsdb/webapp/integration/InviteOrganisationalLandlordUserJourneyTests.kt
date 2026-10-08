package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.TeamMembersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteOrganisationalLandlordUserJourneyPages.InviteOrganisationalLandlordUserFormPage
import kotlin.test.assertEquals

@WithOrgLandlordProfile
class InviteOrganisationalLandlordUserJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var organisationalLandlordInvitationRepository: OrganisationalLandlordInvitationRepository

    @Test
    fun `an admin can invite a team member and sees a success banner, which clears on refresh`(page: Page) {
        var teamMembersPage = navigator.goToTeamMembers()
        assertThat(teamMembersPage.successBanner).isHidden()
        teamMembersPage.inviteTeamMemberButton.clickAndWait()

        val invitePage = assertPageIs(page, InviteOrganisationalLandlordUserFormPage::class)
        invitePage.submitInvitation("new.member@example.com", OrganisationalLandlordUserRole.EDITOR)

        teamMembersPage = assertPageIs(page, TeamMembersPage::class)
        // TODO PDJB-1757: Assert the invitation is shown on the invitations tab
        assertThat(teamMembersPage.successBanner.title).containsText("Success")
        assertThat(teamMembersPage.successBanner.content.heading).containsText("Invitation sent")
        val invitation =
            organisationalLandlordInvitationRepository.findAll().single { it.invitedEmail == "new.member@example.com" }
        assertEquals(OrganisationalLandlordUserRole.EDITOR, invitation.role)
        assertEquals("Local Organisation Landlord", invitation.organisationalLandlord.name)

        page.reload()
        assertThat(teamMembersPage.successBanner).isHidden()
    }
}
