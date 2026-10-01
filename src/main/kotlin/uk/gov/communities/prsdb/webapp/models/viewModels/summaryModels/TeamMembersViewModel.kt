package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser

data class TeamMembersViewModel(
    val adminRows: List<SummaryListRowViewModel>,
    val editorRows: List<SummaryListRowViewModel>,
    val canManageTeamMembers: Boolean,
) {
    companion object {
        fun fromOrganisationalLandlordUsers(
            members: List<OrganisationalLandlordUser>,
            currentUserId: String,
        ): TeamMembersViewModel {
            val canManageTeamMembers =
                members.single { it.baseUser.id == currentUserId }.role == OrganisationalLandlordUserRole.ADMIN

            fun rowsForRole(
                role: OrganisationalLandlordUserRole,
                rowHeadingKey: String,
            ) = members
                .filter { it.role == role }
                .sortedBy { it.name.lowercase() }
                .mapIndexed { index, member ->
                    createRow(member, index + 1, rowHeadingKey, member.baseUser.id == currentUserId, canManageTeamMembers)
                }

            return TeamMembersViewModel(
                adminRows = rowsForRole(OrganisationalLandlordUserRole.ADMIN, "teamMembers.administrators.rowHeading"),
                editorRows = rowsForRole(OrganisationalLandlordUserRole.EDITOR, "teamMembers.editors.rowHeading"),
                canManageTeamMembers = canManageTeamMembers,
            )
        }

        private fun createRow(
            member: OrganisationalLandlordUser,
            rowNumber: Int,
            rowHeadingKey: String,
            isCurrentUser: Boolean,
            canManageTeamMembers: Boolean,
        ) = SummaryListRowViewModel(
            fieldHeading = rowHeadingKey,
            optionalFieldHeadingParam = rowNumber,
            fieldValue = if (isCurrentUser) "teamMembers.currentUserName" else member.name,
            optionalFieldValueParam = if (isCurrentUser) member.name else null,
            actions =
                when {
                    isCurrentUser -> listOf(SummaryListRowActionsViewModel("teamMembers.notAvailable", null))
                    // TODO PDJB-1763: link to the change team member journey
                    canManageTeamMembers -> listOf(SummaryListRowActionsViewModel("forms.links.change", "#"))
                    else -> emptyList()
                },
        )
    }
}
