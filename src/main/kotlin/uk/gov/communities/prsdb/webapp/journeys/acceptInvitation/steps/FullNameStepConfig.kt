package uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.NAME_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("acceptInvitationFullNameStepConfig")
class FullNameStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, AcceptInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    // TODO PDJB-1772: Invitees must give their full name
    override fun getStepSpecificContent(state: AcceptInvitationJourneyState): Map<String, Any?> =
        mapOf("todoComment" to "What is your full name? (TODO PDJB-1772)")

    override fun chooseTemplate(state: AcceptInvitationJourneyState) = "forms/todo"

    override fun mode(state: AcceptInvitationJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("acceptInvitationFullNameStep")
final class FullNameStep(
    stepConfig: FullNameStepConfig,
) : RequestableStep<Complete, NoInputFormModel, AcceptInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = NAME_PATH_SEGMENT
    }
}
