package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.TeamMembersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteTeamMemberJourneyPages.InviteTeamMemberFormPage
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordInvitationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import kotlin.test.assertEquals

@WithOrgLandlordProfile
class InviteTeamMemberJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var organisationalLandlordInvitationRepository: OrganisationalLandlordInvitationRepository

    @Autowired
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @MockitoBean
    private lateinit var invitationEmailSender: EmailNotificationService<OrganisationalLandlordInvitationEmail>

    @Test
    fun `an admin can invite a team member from the team members page and the invitee is emailed`(page: Page) {
        // 1. Team members page
        val teamMembersPage = navigator.goToTeamMembers()
        teamMembersPage.inviteTeamMemberButton.clickAndWait()

        // 2. Invite a team member page
        val invitePage = assertPageIs(page, InviteTeamMemberFormPage::class)
        invitePage.submitInvitation("new.member@example.com", OrganisationalLandlordUserRole.EDITOR)

        // 3. Back on the team members page, with the invitation stored and emailed
        assertPageIs(page, TeamMembersPage::class)
        val invitation =
            organisationalLandlordInvitationRepository.findAll().single { it.invitedEmail == "new.member@example.com" }
        assertEquals(OrganisationalLandlordUserRole.EDITOR, invitation.role)
        assertEquals("Local Organisation Landlord", invitation.organisationalLandlord.name)
        verify(invitationEmailSender).sendEmail(
            "new.member@example.com",
            OrganisationalLandlordInvitationEmail(
                organisationName = "Local Organisation Landlord",
                invitationUri = absoluteUrlProvider.buildOrganisationalLandlordInvitationUri(invitation.token.toString()),
            ),
        )
    }
}
