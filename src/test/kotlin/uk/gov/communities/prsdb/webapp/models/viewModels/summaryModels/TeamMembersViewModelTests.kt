package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole.ADMIN
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole.EDITOR
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TeamMembersViewModelTests {
    private val currentUserId = "current-user"
    private val organisation = MockLandlordData.createOrgLandlord()

    private fun member(
        name: String,
        role: OrganisationalLandlordUserRole,
        id: String = name,
    ) = OrganisationalLandlordUser(organisation, PrsdbUser(id), name, "$id@example.com", role)

    private fun invitation(
        email: String,
        daysAgo: Long,
        isHidden: Boolean = false,
        id: Long = 0,
    ) = MockLandlordData.createOrganisationalLandlordInvitation(
        id = id,
        invitedEmail = email,
        organisationalLandlord = organisation,
        createdDate = Instant.now().minus(daysAgo, ChronoUnit.DAYS),
        isHidden = isHidden,
    )

    @Test
    fun `members are split by role, sorted alphabetically ignoring case and numbered in order`() {
        // Arrange
        val members =
            listOf(
                member("zara Admin", ADMIN),
                member("Current User", ADMIN, currentUserId),
                member("alex Admin", ADMIN),
                member("Ed Editor", EDITOR),
                member("Bea Editor", EDITOR),
            )

        // Act
        val viewModel = TeamMembersViewModel.fromMembersAndInvitations(members, emptyList(), currentUserId)

        // Assert
        assertEquals(
            listOf("alex Admin", "teamMembers.currentUserName", "zara Admin"),
            viewModel.adminRows.map { it.fieldValue },
        )
        assertEquals(listOf(1, 2, 3), viewModel.adminRows.map { it.optionalFieldHeadingParam })
        assertEquals("teamMembers.administrators.rowHeading", viewModel.adminRows.first().fieldHeading)
        assertEquals(listOf("Bea Editor", "Ed Editor"), viewModel.editorRows.map { it.fieldValue })
        assertEquals("teamMembers.editors.rowHeading", viewModel.editorRows.first().fieldHeading)
    }

    @Test
    fun `the current user's row shows their name with a You suffix`() {
        val viewModel =
            TeamMembersViewModel.fromMembersAndInvitations(
                listOf(member("Current User", ADMIN, currentUserId)),
                emptyList(),
                currentUserId,
            )

        val row = viewModel.adminRows.single()
        assertEquals("teamMembers.currentUserName", row.fieldValue)
        assertEquals("Current User", row.optionalFieldValueParam)
    }

    @Test
    fun `pending invitations are listed oldest first, numbered, with Resend and Cancel actions`() {
        // Arrange
        val invitations =
            listOf(
                invitation("newer@example.com", daysAgo = 1),
                invitation("older@example.com", daysAgo = 10),
            )

        // Act
        val viewModel = TeamMembersViewModel.fromMembersAndInvitations(emptyList(), invitations, currentUserId)

        // Assert
        assertEquals(
            listOf("older@example.com", "newer@example.com"),
            viewModel.pendingInvitationRows.map { it.fieldValue },
        )
        assertEquals(listOf(1, 2), viewModel.pendingInvitationRows.map { it.optionalFieldHeadingParam })
        assertEquals("teamMembers.invitations.rowHeading", viewModel.pendingInvitationRows.first().fieldHeading)
        assertEquals(
            listOf(
                SummaryListRowActionsViewModel("forms.links.resend", "#"),
                SummaryListRowActionsViewModel("forms.links.cancel", "#"),
            ),
            viewModel.pendingInvitationRows.first().actions,
        )
        assertTrue(viewModel.expiredInvitationRows.isEmpty())
    }

    @Test
    fun `expired invitations are listed separately, oldest first, with Resend and Remove actions`() {
        // Arrange
        val invitations =
            listOf(
                invitation("pending@example.com", daysAgo = 1, id = 1),
                invitation("expired-newer@example.com", daysAgo = 40, id = 2),
                invitation("expired-older@example.com", daysAgo = 60, id = 3),
            )

        // Act
        val viewModel = TeamMembersViewModel.fromMembersAndInvitations(emptyList(), invitations, currentUserId)

        // Assert
        assertEquals(listOf("pending@example.com"), viewModel.pendingInvitationRows.map { it.fieldValue })
        assertEquals(
            listOf("expired-older@example.com", "expired-newer@example.com"),
            viewModel.expiredInvitationRows.map { it.fieldValue },
        )
        assertEquals(listOf(1, 2), viewModel.expiredInvitationRows.map { it.optionalFieldHeadingParam })
        assertEquals("teamMembers.invitations.expired.rowHeading", viewModel.expiredInvitationRows.first().fieldHeading)
        assertEquals(
            listOf(
                SummaryListRowActionsViewModel("forms.links.resend", "#"),
                SummaryListRowActionsViewModel("forms.links.remove", "/landlord/team-members/remove-expired-invite/3"),
            ),
            viewModel.expiredInvitationRows.first().actions,
        )
        assertEquals(
            "/landlord/team-members/remove-expired-invite/2",
            viewModel.expiredInvitationRows
                .last()
                .actions
                .last()
                .url,
        )
    }

    @Test
    fun `hidden invitations are not listed`() {
        // Arrange
        val invitations =
            listOf(
                invitation("hidden-pending@example.com", daysAgo = 1, isHidden = true),
                invitation("hidden-expired@example.com", daysAgo = 60, isHidden = true),
            )

        // Act
        val viewModel = TeamMembersViewModel.fromMembersAndInvitations(emptyList(), invitations, currentUserId)

        // Assert
        assertTrue(viewModel.pendingInvitationRows.isEmpty())
        assertTrue(viewModel.expiredInvitationRows.isEmpty())
    }
}
