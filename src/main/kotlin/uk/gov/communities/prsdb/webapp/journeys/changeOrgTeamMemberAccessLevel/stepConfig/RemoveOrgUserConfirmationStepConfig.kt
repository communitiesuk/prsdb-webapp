package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class RemoveOrgUserConfirmationStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, ChangeOrgTeamMemberAccessLevelJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: ChangeOrgTeamMemberAccessLevelJourneyState): Map<String, Any?> =
        mapOf("todoComment" to "Confirm removal from the organisation (TODO PDJB-1764)")

    override fun chooseTemplate(state: ChangeOrgTeamMemberAccessLevelJourneyState) = "forms/todo"

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
