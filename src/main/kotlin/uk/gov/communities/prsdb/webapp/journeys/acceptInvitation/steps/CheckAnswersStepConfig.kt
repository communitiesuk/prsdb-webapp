package uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("acceptInvitationCheckAnswersStepConfig")
class CheckAnswersStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, AcceptInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    // TODO PDJB-1774: Invitees can check their answers
    override fun getStepSpecificContent(state: AcceptInvitationJourneyState): Map<String, Any?> =
        mapOf("todoComment" to "Check your answers (TODO PDJB-1774)")

    override fun chooseTemplate(state: AcceptInvitationJourneyState) = "forms/todo"

    override fun mode(state: AcceptInvitationJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("acceptInvitationCheckAnswersStep")
final class CheckAnswersStep(
    stepConfig: CheckAnswersStepConfig,
) : RequestableStep<Complete, NoInputFormModel, AcceptInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-your-answers"
    }
}
