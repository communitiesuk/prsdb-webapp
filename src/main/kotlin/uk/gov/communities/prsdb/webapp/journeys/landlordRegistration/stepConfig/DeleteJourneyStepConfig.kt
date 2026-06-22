package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class DeleteJourneyStepConfig() : AbstractInternalStepConfig<Complete, JourneyState>() {
    override fun mode(state: JourneyState): Complete = Complete.COMPLETE

    override fun resolveNextDestination(
        state: JourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class DeleteJourneyStep(
    stepConfig: DeleteJourneyStepConfig,
) : JourneyStep.InternalStep<Complete, JourneyState>(stepConfig)
