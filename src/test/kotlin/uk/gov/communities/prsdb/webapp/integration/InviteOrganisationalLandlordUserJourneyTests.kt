package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.TeamMembersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteOrganisationalLandlordUserJourneyPages.InviteOrganisationalLandlordUserFormPage
import kotlin.test.assertEquals

@WithOrgLandlordProfile
class InviteOrganisationalLandlordUserJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var organisationalLandlordInvitationRepository: OrganisationalLandlordInvitationRepository

    @Test
    fun `an admin can invite a team member from the team members page`(page: Page) {
        val teamMembersPage = navigator.goToTeamMembers()
        teamMembersPage.inviteTeamMemberButton.clickAndWait()

        val invitePage = assertPageIs(page, InviteOrganisationalLandlordUserFormPage::class)
        invitePage.submitInvitation("new.member@example.com", OrganisationalLandlordUserRole.EDITOR)

        assertPageIs(page, TeamMembersPage::class)
        // TODO PDJB-1757: Assert the invitation is shown on the invitations tab
        // TODO PDJB-1762: Assert the success banner is shown
        val invitation =
            organisationalLandlordInvitationRepository.findAll().single { it.invitedEmail == "new.member@example.com" }
        assertEquals(OrganisationalLandlordUserRole.EDITOR, invitation.role)
        assertEquals("Local Organisation Landlord", invitation.organisationalLandlord.name)
    }
}
