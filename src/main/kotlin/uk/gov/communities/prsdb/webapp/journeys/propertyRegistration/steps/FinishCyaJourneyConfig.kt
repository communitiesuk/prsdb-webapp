package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

// TODO PDJB-579: Revert to CYA State
@JourneyFrameworkComponent
class FinishCyaJourneyConfig(
    private val stateFactory: ObjectFactory<PropertyRegistrationJourneyState>,
) : AbstractInternalStepConfig<Complete, PropertyRegistrationJourneyState>() {
    override fun mode(state: PropertyRegistrationJourneyState) = Complete.COMPLETE

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        val originalId = state.baseJourneyId
        val destination = Destination.ExternalUrl("check-answers", mapOf("journeyId" to originalId))
        state.copyJourneyTo(originalId)
        val originalState = stateFactory.getObject().apply { setJourneyId(originalId) }
        originalState.checkingAnswersFor = null
        state.checkingAnswersFor?.let { originalState.cyaJourneys -= it }
        state.deleteJourney()
        return destination
    }
}

@JourneyFrameworkComponent
class FinishCyaJourneyStep(
    config: FinishCyaJourneyConfig,
) : JourneyStep.InternalStep<Complete, PropertyRegistrationJourneyState>(config)
