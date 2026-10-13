package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.TeamMembersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteOrganisationalLandlordUserJourneyPages.InviteOrganisationalLandlordUserFormPage
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordInvitationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import kotlin.test.assertEquals

@WithOrgLandlordProfile
class InviteOrganisationalLandlordUserJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var organisationalLandlordInvitationRepository: OrganisationalLandlordInvitationRepository

    @Autowired
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @MockitoBean
    private lateinit var invitationEmailSender: EmailNotificationService<OrganisationalLandlordInvitationEmail>

    @Test
    fun `an admin can invite a team member from the team members page and the invitee is emailed`(page: Page) {
        val teamMembersPage = navigator.goToTeamMembers()
        teamMembersPage.inviteTeamMemberButton.clickAndWait()

        val invitePage = assertPageIs(page, InviteOrganisationalLandlordUserFormPage::class)
        invitePage.submitInvitation("new.member@example.com", OrganisationalLandlordUserRole.EDITOR)

        assertPageIs(page, TeamMembersPage::class)
        // TODO PDJB-1757: Assert the invitation is shown on the invitations tab
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

    @Test
    fun `an admin sees a success banner after inviting a team member, which clears on refresh`(page: Page) {
        var teamMembersPage = navigator.goToTeamMembers()
        assertThat(teamMembersPage.successBanner).isHidden()
        teamMembersPage.inviteTeamMemberButton.clickAndWait()

        val invitePage = assertPageIs(page, InviteOrganisationalLandlordUserFormPage::class)
        invitePage.submitInvitation("new.member@example.com", OrganisationalLandlordUserRole.EDITOR)

        teamMembersPage = assertPageIs(page, TeamMembersPage::class)
        assertThat(teamMembersPage.successBanner.content.heading).containsText("Invitation sent")

        page.reload()
        assertThat(teamMembersPage.successBanner).isHidden()
    }
}
