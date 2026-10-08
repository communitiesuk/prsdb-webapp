package uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.InviteTeamMemberJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteTeamMemberFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent
class InviteTeamMemberStepConfig(
    private val userToLandlordService: UserToLandlordService,
) : AbstractRequestableStepConfig<Complete, InviteTeamMemberFormModel, InviteTeamMemberJourneyState>() {
    override val formModelClass = InviteTeamMemberFormModel::class

    override fun getStepSpecificContent(state: InviteTeamMemberJourneyState): Map<String, Any?> =
        mapOf(
            "organisationName" to userToLandlordService.getCurrentOrganisationLandlordForUser().name,
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = OrganisationalLandlordUserRole.ADMIN,
                        labelMsgKey = "inviteTeamMember.role.radios.admin.label",
                        hintMsgKey = "inviteTeamMember.role.radios.admin.hint",
                    ),
                    RadiosButtonViewModel(
                        value = OrganisationalLandlordUserRole.EDITOR,
                        labelMsgKey = "inviteTeamMember.role.radios.editor.label",
                        hintMsgKey = "inviteTeamMember.role.radios.editor.hint",
                    ),
                ),
        )

    override fun chooseTemplate(state: InviteTeamMemberJourneyState) = "forms/inviteTeamMemberForm"

    override fun mode(state: InviteTeamMemberJourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            Complete.COMPLETE
        }
}

@JourneyFrameworkComponent
final class InviteTeamMemberStep(
    stepConfig: InviteTeamMemberStepConfig,
) : RequestableStep<Complete, InviteTeamMemberFormModel, InviteTeamMemberJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "invite-team-member"
    }
}
