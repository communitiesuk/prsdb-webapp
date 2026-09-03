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
    private lateinit var isDelegatedToLettingAgent: () -> Boolean

    fun usingCurrentDelegation(isDelegatedToLettingAgent: () -> Boolean): OccupancyUpdateRoutingStepConfig {
        this.isDelegatedToLettingAgent = isDelegatedToLettingAgent
        return this
    }

    override fun isSubClassInitialised() = ::isDelegatedToLettingAgent.isInitialized

    override fun mode(state: UpdateOccupancyJourneyState): OccupancyUpdateRouteMode? {
        val newOccupancy = state.occupied.outcome ?: return null
        return if (newOccupancy == YesOrNo.NO && isDelegatedToLettingAgent()) {
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
