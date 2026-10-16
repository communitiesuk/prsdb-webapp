package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.controllers.ChangeOrgTeamMemberAccessLevelController
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser

data class TeamMembersViewModel(
    val adminRows: List<SummaryListRowViewModel>,
    val editorRows: List<SummaryListRowViewModel>,
    val pendingInvitationRows: List<SummaryListRowViewModel>,
    val expiredInvitationRows: List<SummaryListRowViewModel>,
) {
    companion object {
        fun fromMembersAndInvitations(
            members: List<OrganisationalLandlordUser>,
            invitations: List<OrganisationalLandlordInvitation>,
            currentUserId: String,
        ): TeamMembersViewModel {
            fun rowsForRole(
                role: OrganisationalLandlordUserRole,
                rowHeadingKey: String,
            ) = members
                .filter { it.role == role }
                .sortedBy { it.name.lowercase() }
                .mapIndexed { index, member ->
                    createRow(
                        member,
                        index + 1,
                        rowHeadingKey,
                        member.baseUser.id == currentUserId,
                    )
                }

            fun invitationsWithStatus(status: OrganisationalLandlordInvitationStatus) =
                invitations
                    .filter { it.status == status }
                    .sortedBy { it.createdDate }

            return TeamMembersViewModel(
                adminRows = rowsForRole(OrganisationalLandlordUserRole.ADMIN, "teamMembers.administrators.rowHeading"),
                editorRows = rowsForRole(OrganisationalLandlordUserRole.EDITOR, "teamMembers.editors.rowHeading"),
                pendingInvitationRows =
                    invitationsWithStatus(OrganisationalLandlordInvitationStatus.PENDING)
                        .mapIndexed { index, invitation -> createPendingInvitationRow(invitation, index + 1) },
                expiredInvitationRows =
                    invitationsWithStatus(OrganisationalLandlordInvitationStatus.EXPIRED)
                        .mapIndexed { index, invitation -> createExpiredInvitationRow(invitation, index + 1) },
            )
        }

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
            actions =
                if (isCurrentUser) {
                    emptyList()
                } else {
                    listOf(
                        SummaryListRowActionsViewModel(
                            "forms.links.change",
                            ChangeOrgTeamMemberAccessLevelController.getChangeAccessLevelPath(member.id),
                        ),
                    )
                },
        )

        private fun createPendingInvitationRow(
            invitation: OrganisationalLandlordInvitation,
            rowNumber: Int,
        ) = SummaryListRowViewModel(
            fieldHeading = "teamMembers.invitations.rowHeading",
            optionalFieldHeadingParam = rowNumber,
            fieldValue = invitation.invitedEmail,
            actions =
                listOf(
                    SummaryListRowActionsViewModel("forms.links.resend", TeamMembersController.getResendInvitationPath(invitation.id)),
                    // TODO PDJB-1769: replace "#" with the cancel invitation URL
                    SummaryListRowActionsViewModel("forms.links.cancel", "#"),
                ),
        )

        private fun createExpiredInvitationRow(
            invitation: OrganisationalLandlordInvitation,
            rowNumber: Int,
        ) = SummaryListRowViewModel(
            fieldHeading = "teamMembers.invitations.expired.rowHeading",
            optionalFieldHeadingParam = rowNumber,
            fieldValue = invitation.invitedEmail,
            actions =
                listOf(
                    SummaryListRowActionsViewModel("forms.links.resend", TeamMembersController.getResendInvitationPath(invitation.id)),
                    // TODO PDJB-1854: replace "#" with the remove expired invitation URL
                    SummaryListRowActionsViewModel("forms.links.remove", "#"),
                ),
        )
    }
}
