package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

enum class OccupancyChangeRouteMode {
    NO_INTERRUPTION,
    REMOVING_DELEGATION,
}

@JourneyFrameworkComponent
class OccupancyChangeRoutingStepConfig :
    AbstractInternalStepConfig<OccupancyChangeRouteMode, PropertyRegistrationJourneyState>() {
    private lateinit var wasDelegatedToLettingAgent: () -> Boolean

    fun usingPreviousDelegation(wasDelegatedToLettingAgent: () -> Boolean): OccupancyChangeRoutingStepConfig {
        this.wasDelegatedToLettingAgent = wasDelegatedToLettingAgent
        return this
    }

    fun getWasDelegatedToLettingAgentFromBaseJourney(state: PropertyRegistrationJourneyState): Boolean {
        val baseState = state.getBaseJourneyState() as PropertyRegistrationJourneyState
        return baseState.cachedOccupied == true &&
            baseState.whoProvidesDetailsTask.cachedWhoProvidesRentalDetails == WhoProvidesRentalDetails.LETTING_AGENT
    }

    override fun isSubClassInitialised() = ::wasDelegatedToLettingAgent.isInitialized

    override fun mode(state: PropertyRegistrationJourneyState): OccupancyChangeRouteMode? {
        val newOccupancy = state.occupied.outcome ?: return null
        return if (wasDelegatedToLettingAgent() && newOccupancy == YesOrNo.NO) {
            OccupancyChangeRouteMode.REMOVING_DELEGATION
        } else {
            OccupancyChangeRouteMode.NO_INTERRUPTION
        }
    }
}

@JourneyFrameworkComponent
class OccupancyChangeRoutingStep(
    stepConfig: OccupancyChangeRoutingStepConfig,
) : InternalStep<OccupancyChangeRouteMode, PropertyRegistrationJourneyState>(stepConfig)
