package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.HasPropertyId
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateSuccessBannerService

/**
 * Shared terminal step for all letting-agent-reachable property update journeys.
 * Records a one-time success banner entry keyed by propertyId, then deletes the
 * journey. Must be the final step reached after a journey's actual data-persisting
 * step (Apply<X>UpdateStep or a CYA step), which must NOT delete the journey
 * themselves so that propertyId can still be read here.
 */
@JourneyFrameworkComponent
class CompletePropertyUpdateStepConfig(
    private val propertyUpdateSuccessBannerService: PropertyUpdateSuccessBannerService,
) : AbstractInternalStepConfig<Complete, JourneyState>() {
    override fun mode(state: JourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: JourneyState) {
        val hasPropertyIdState = state as HasPropertyId
        propertyUpdateSuccessBannerService.markSuccess(hasPropertyIdState.propertyId, hasPropertyIdState.successBannerMessageKey)
        state.deleteJourney()
    }

    override fun resolveNextDestination(
        state: JourneyState,
        defaultDestination: Destination,
    ): Destination = defaultDestination
}

@JourneyFrameworkComponent
class CompletePropertyUpdateStep(
    stepConfig: CompletePropertyUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, JourneyState>(stepConfig)
