package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole.ADMIN
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole.EDITOR
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TeamMembersViewModelTests {
    private val currentUserId = "current-user"
    private val organisation = MockLandlordData.createOrgLandlord()

    private fun member(
        name: String,
        role: OrganisationalLandlordUserRole,
        id: String = name,
    ) = OrganisationalLandlordUser(organisation, PrsdbUser(id), name, "$id@example.com", role)

    @Test
    fun `members are split by role, sorted alphabetically ignoring case and numbered in order`() {
        val viewModel =
            TeamMembersViewModel.fromOrganisationalLandlordUsers(
                listOf(
                    member("zara Admin", ADMIN),
                    member("Current User", ADMIN, currentUserId),
                    member("alex Admin", ADMIN),
                    member("Ed Editor", EDITOR),
                    member("Bea Editor", EDITOR),
                ),
                currentUserId,
            )

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
    fun `the current user's row shows their name with a You suffix and a Not available action without a link`() {
        val viewModel =
            TeamMembersViewModel.fromOrganisationalLandlordUsers(
                listOf(member("Current User", ADMIN, currentUserId)),
                currentUserId,
            )

        val row = viewModel.adminRows.single()
        assertEquals("teamMembers.currentUserName", row.fieldValue)
        assertEquals("Current User", row.optionalFieldValueParam)
        assertEquals(listOf(SummaryListRowActionsViewModel("teamMembers.notAvailable", null)), row.actions)
    }

    @Test
    fun `an admin viewer can manage team members and gets a Change action on other members' rows`() {
        val viewModel =
            TeamMembersViewModel.fromOrganisationalLandlordUsers(
                listOf(member("Current User", ADMIN, currentUserId), member("Other Editor", EDITOR)),
                currentUserId,
            )

        assertTrue(viewModel.canManageTeamMembers)
        assertEquals(listOf(SummaryListRowActionsViewModel("forms.links.change", "#")), viewModel.editorRows.single().actions)
    }

    @Test
    fun `an editor viewer cannot manage team members and gets no actions on other members' rows`() {
        val viewModel =
            TeamMembersViewModel.fromOrganisationalLandlordUsers(
                listOf(member("Other Admin", ADMIN), member("Current User", EDITOR, currentUserId)),
                currentUserId,
            )

        assertFalse(viewModel.canManageTeamMembers)
        assertEquals(emptyList<SummaryListRowActionsViewModel>(), viewModel.adminRows.single().actions)
    }
}
