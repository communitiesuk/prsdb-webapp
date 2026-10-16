package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.RESEND_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.ADMINISTRATORS_FRAGMENT
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.INVITATIONS_FRAGMENT
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.TeamMembersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordInvitationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import java.util.UUID
import java.util.regex.Pattern
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@WithOrgLandlordProfile
class TeamMembersTests :
    IntegrationTestWithImmutableData(listOf("data-local.sql", "data-org-landlord-team-members.sql")) {
    @Autowired
    private lateinit var invitationRepository: OrganisationalLandlordInvitationRepository

    @Autowired
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @MockitoBean
    private lateinit var invitationEmailSender: EmailNotificationService<OrganisationalLandlordInvitationEmail>

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
                assertThat(row.actions.getActionLink("Resend")).hasAttribute("href", resendInvitationHrefPattern)
                assertThat(row.actions.getActionLink("Cancel")).isVisible()
                // TODO PDJB-1769: assert the Cancel link goes to the cancel invitation page
            }

        val expiredInvitations = teamMembersPage.expiredInvitationsSummaryList
        assertThat(expiredInvitations.rows).hasCount(1)
        val expiredRow = expiredInvitations.getRowByIndex(0)
        assertThat(expiredRow.key).hasText("Expired invite 1")
        assertThat(expiredRow.value).hasText("expired-org-invite-d@example.com")
        assertThat(expiredRow.actions.getActionLink("Resend")).hasAttribute("href", resendInvitationHrefPattern)
        assertThat(expiredRow.actions.getActionLink("Remove")).isVisible()
        // TODO PDJB-1854: assert the Remove link goes to the remove expired invitation page

        assertThat(teamMembersPage.invitationsPanel).not().containsText("hidden-org-invite-e@example.com")
        val hiddenInvitation = invitationRepository.findByToken(UUID.fromString("1234abcd-5678-abcd-1234-567abcd2222e"))
        assertNotNull(hiddenInvitation)
        assertTrue(hiddenInvitation.isHidden)
    }

    @Nested
    inner class ResendInvitation :
        IntegrationTestWithMutableData.NestedIntegrationTestWithMutableData(
            listOf("data-local.sql", "data-org-landlord-team-members.sql"),
        ) {
        @Test
        fun `resending a pending invitation emails the invitee, shows a banner and moves the invitation to the end of the list`(
            page: Page,
        ) {
            // Arrange
            val teamMembersPage = navigator.goToTeamMembers()
            teamMembersPage.tabs.goToInvitations(pendingInvitationCount = 3)

            // Act
            teamMembersPage.pendingInvitationsSummaryList
                .getRowByIndex(0)
                .actions
                .getActionLink("Resend")
                .clickAndWait()

            // Assert
            val resultPage = BasePage.assertPageIs(page, TeamMembersPage::class)
            assertThat(resultPage.successBanner.title).hasText("Success")
            assertThat(resultPage.successBanner.content.heading).hasText("Invitation resent")
            assertThat(resultPage.successBanner.content).containsText("An invitation has been sent to pending-org-invite-c@example.com")
            assertEquals(INVITATIONS_FRAGMENT, resultPage.tabs.activeTabPanelId)
            assertThat(resultPage.pendingInvitationsSummaryList.getRowByIndex(2).value).hasText("pending-org-invite-c@example.com")
            verify(invitationEmailSender).sendEmail(
                "pending-org-invite-c@example.com",
                OrganisationalLandlordInvitationEmail(
                    organisationName = "Local Organisation Landlord",
                    invitationUri = absoluteUrlProvider.buildOrganisationalLandlordInvitationUri("1234abcd-5678-abcd-1234-567abcd2222c"),
                ),
            )
        }

        @Test
        fun `resending an expired invitation moves it to the pending invitations`(page: Page) {
            // Arrange
            val teamMembersPage = navigator.goToTeamMembers()
            teamMembersPage.tabs.goToInvitations(pendingInvitationCount = 3)

            // Act
            teamMembersPage.expiredInvitationsSummaryList
                .getRowByIndex(0)
                .actions
                .getActionLink("Resend")
                .clickAndWait()

            // Assert
            val resultPage = BasePage.assertPageIs(page, TeamMembersPage::class)
            assertThat(resultPage.successBanner.content).containsText("An invitation has been sent to expired-org-invite-d@example.com")
            assertThat(resultPage.pendingInvitationsSummaryList.rows).hasCount(4)
            assertThat(resultPage.expiredInvitationsSummaryList).isHidden()
        }
    }

    companion object {
        private val resendInvitationHrefPattern = Pattern.compile("^$TEAM_MEMBERS_ROUTE/$RESEND_PATH_SEGMENT/\\d+$")
    }
}
