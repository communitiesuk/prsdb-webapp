package uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.LettingAgentInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService

@JourneyFrameworkComponent
class StartStepConfig(
    private val lettingAgentAccessService: LettingAgentAccessService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, LettingAgentInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: LettingAgentInvitationJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: LettingAgentInvitationJourneyState): String = ""

    override fun mode(state: LettingAgentInvitationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: LettingAgentInvitationJourneyState) {
        state.invitationToken = lettingAgentAccessService.getInvitationTokenForJourneyIdFromSession(state.journeyId)
    }
}

@JourneyFrameworkComponent
final class StartStep(
    stepConfig: StartStepConfig,
) : RequestableStep<Complete, NoInputFormModel, LettingAgentInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "start"
    }
}
