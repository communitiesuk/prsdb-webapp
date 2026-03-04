package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState

@JourneyFrameworkComponent
class FinishCyaJourneyConfig : AbstractInternalStepConfig<Complete, CheckYourAnswersJourneyState>() {
    override fun mode(state: CheckYourAnswersJourneyState) = Complete.COMPLETE

    override fun resolveNextDestination(
        state: CheckYourAnswersJourneyState,
        defaultDestination: Destination,
    ): Destination {
        val originalId = state.baseJourneyId
        val destination = state.returnToCyaPageDestination
        state.copyJourneyTo(originalId)
        val originalState = state.getBaseJourneyState()
        originalState.checkingAnswersFor = null
        originalState.cyaJourneys -= state.checkingAnswersFor!!
        state.checkingAnswersFor?.let { originalState.cyaJourneys -= it }
        state.deleteJourney()
        return destination
    }
}

@JourneyFrameworkComponent
class FinishCyaJourneyStep(
    config: FinishCyaJourneyConfig,
) : JourneyStep.InternalStep<Complete, CheckYourAnswersJourneyState>(config)
