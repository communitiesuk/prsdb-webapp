package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState

@JourneyFrameworkComponent
class FinishCyaJourneyConfig<TCheckableElements : Enum<TCheckableElements>>(
    private val stateFactory: ObjectFactory<CheckYourAnswersJourneyState<TCheckableElements>>,
) : AbstractInternalStepConfig<Complete, CheckYourAnswersJourneyState<TCheckableElements>>() {
    override fun mode(state: CheckYourAnswersJourneyState<TCheckableElements>) = Complete.COMPLETE

    override fun resolveNextDestination(
        state: CheckYourAnswersJourneyState<TCheckableElements>,
        defaultDestination: Destination,
    ): Destination {
        val originalId = state.baseJourneyId
        val destination = state.returnToCyaPageDestination
        state.copyJourneyTo(originalId)
        val originalState = stateFactory.getObject().apply { setJourneyId(originalId) }
        originalState.checkingAnswersFor = null
        originalState.cyaJourneys -= state.checkingAnswersFor!!
        state.checkingAnswersFor?.let { originalState.cyaJourneys -= it }
        state.deleteJourney()
        return destination
    }
}

@JourneyFrameworkComponent
class FinishCyaJourneyStep<TCheckableElements : Enum<TCheckableElements>>(
    config: FinishCyaJourneyConfig<TCheckableElements>,
) : JourneyStep.InternalStep<Complete, CheckYourAnswersJourneyState<TCheckableElements>>(config)
