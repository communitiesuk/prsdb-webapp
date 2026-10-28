package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService

@JourneyFrameworkComponent
class RemoveOrgUserConfirmationStepConfig(
    private val organisationalLandlordUserService: OrganisationalLandlordUserService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, ChangeOrgTeamMemberAccessLevelJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: ChangeOrgTeamMemberAccessLevelJourneyState): Map<String, Any?> =
        mapOf(
            "memberName" to
                organisationalLandlordUserService.getOrganisationalLandlordUserOrNull(state.otherOrganisationalLandlordUserId)?.name,
            "cancelLinkUrl" to TeamMembersController.TEAM_MEMBERS_ROUTE,
        )

    override fun chooseTemplate(state: ChangeOrgTeamMemberAccessLevelJourneyState) = "forms/removeOrgUserConfirmationForm"

    override fun mode(state: ChangeOrgTeamMemberAccessLevelJourneyState): Complete? =
        getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class RemoveOrgUserConfirmationStep(
    stepConfig: RemoveOrgUserConfirmationStepConfig,
) : RequestableStep<Complete, NoInputFormModel, ChangeOrgTeamMemberAccessLevelJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "confirm-removal"
    }
}
