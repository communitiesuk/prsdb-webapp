package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

enum class OccupancyUpdateRouteMode {
    NO_INTERRUPTION,
    SHOW_INTERRUPTION,
}

@JourneyFrameworkComponent
class OccupancyUpdateRoutingStepConfig : AbstractInternalStepConfig<OccupancyUpdateRouteMode, UpdateOccupancyJourneyState>() {
    override fun mode(state: UpdateOccupancyJourneyState): OccupancyUpdateRouteMode? {
        val newOccupancy = state.occupied.outcome ?: return null
        val isRemovingDelegation =
            state.propertyIsOccupied && state.propertyIsDelegatedToLettingAgent && newOccupancy == YesOrNo.NO
        return if (isRemovingDelegation) {
            OccupancyUpdateRouteMode.SHOW_INTERRUPTION
        } else {
            OccupancyUpdateRouteMode.NO_INTERRUPTION
        }
    }
}

@JourneyFrameworkComponent
class OccupancyUpdateRoutingStep(
    stepConfig: OccupancyUpdateRoutingStepConfig,
) : InternalStep<OccupancyUpdateRouteMode, UpdateOccupancyJourneyState>(stepConfig)
