package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser

data class TeamMembersViewModel(
    val adminRows: List<SummaryListRowViewModel>,
    val editorRows: List<SummaryListRowViewModel>,
) {
    companion object {
        fun fromOrganisationalLandlordUsers(
            members: List<OrganisationalLandlordUser>,
            currentUserId: String,
        ): TeamMembersViewModel {
            fun rowsForRole(
                role: OrganisationalLandlordUserRole,
                rowHeadingKey: String,
            ) = members
                .filter { it.role == role }
                .sortedBy { it.name.lowercase() }
                .mapIndexed { index, member -> createRow(member, index + 1, rowHeadingKey, member.baseUser.id == currentUserId) }

            return TeamMembersViewModel(
                adminRows = rowsForRole(OrganisationalLandlordUserRole.ADMIN, "teamMembers.administrators.rowHeading"),
                editorRows = rowsForRole(OrganisationalLandlordUserRole.EDITOR, "teamMembers.editors.rowHeading"),
            )
        }

        // TODO PDJB-1763: add "Change" links to other members' rows for admins, and "Not available" to the current user's row
        private fun createRow(
            member: OrganisationalLandlordUser,
            rowNumber: Int,
            rowHeadingKey: String,
            isCurrentUser: Boolean,
        ) = SummaryListRowViewModel(
            fieldHeading = rowHeadingKey,
            optionalFieldHeadingParam = rowNumber,
            fieldValue = if (isCurrentUser) "teamMembers.currentUserName" else member.name,
            optionalFieldValueParam = if (isCurrentUser) member.name else null,
        )
    }
}
