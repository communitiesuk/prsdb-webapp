package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

// TODO PDJB-2045: reactivate the property in afterStepIsReached
@JourneyFrameworkComponent
class CompleteReactivationStepConfig : AbstractInternalStepConfig<Complete, PropertyReactivationJourneyState>() {
    override fun mode(state: PropertyReactivationJourneyState): Complete = Complete.COMPLETE

    override fun resolveNextDestination(
        state: PropertyReactivationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteReactivationStep(
    stepConfig: CompleteReactivationStepConfig,
) : JourneyStep.InternalStep<Complete, PropertyReactivationJourneyState>(stepConfig)
