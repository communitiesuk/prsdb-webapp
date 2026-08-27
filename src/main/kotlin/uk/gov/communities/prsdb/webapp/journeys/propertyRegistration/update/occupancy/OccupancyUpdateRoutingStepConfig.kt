package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

enum class OccupancyUpdateRouteMode {
    NO_INTERRUPTION,
    REMOVING_DELEGATION,
}

@JourneyFrameworkComponent
class OccupancyUpdateRoutingStepConfig : AbstractInternalStepConfig<OccupancyUpdateRouteMode, UpdateOccupancyJourneyState>() {
    override fun mode(state: UpdateOccupancyJourneyState): OccupancyUpdateRouteMode? {
        val newOccupancy = state.occupied.outcome ?: return null
        return if (state.showsLettingAgentInterruption && newOccupancy == YesOrNo.NO) {
            OccupancyUpdateRouteMode.REMOVING_DELEGATION
        } else {
            OccupancyUpdateRouteMode.NO_INTERRUPTION
        }
    }
}

@JourneyFrameworkComponent
class OccupancyUpdateRoutingStep(
    stepConfig: OccupancyUpdateRoutingStepConfig,
) : InternalStep<OccupancyUpdateRouteMode, UpdateOccupancyJourneyState>(stepConfig)
