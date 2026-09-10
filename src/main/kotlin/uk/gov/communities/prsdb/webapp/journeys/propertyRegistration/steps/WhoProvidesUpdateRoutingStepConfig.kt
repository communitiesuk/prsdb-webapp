package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState

enum class WhoProvidesUpdateRouteMode {
    UNCHANGED,
    CHANGED_TO_LETTING_AGENT,
    CHANGED_TO_LANDLORD,
}

@JourneyFrameworkComponent
class WhoProvidesUpdateRoutingStepConfig :
    AbstractInternalStepConfig<WhoProvidesUpdateRouteMode, PropertyRegistrationJourneyState>() {
    private lateinit var previousIsDelegatedToLettingAgent: () -> Boolean

    fun usingPreviousDelegation(previouslyDelegated: () -> Boolean): WhoProvidesUpdateRoutingStepConfig {
        this.previousIsDelegatedToLettingAgent = previouslyDelegated
        return this
    }

    fun getWasDelegatedToLettingAgentFromBaseJourney(state: PropertyRegistrationJourneyState): Boolean {
        val baseState = state.getBaseJourneyState() as PropertyRegistrationJourneyState
        return baseState.cachedWhoProvidesRentalDetails == WhoProvidesRentalDetails.LETTING_AGENT
    }

    override fun isSubClassInitialised() = ::previousIsDelegatedToLettingAgent.isInitialized

    override fun mode(state: PropertyRegistrationJourneyState): WhoProvidesUpdateRouteMode? {
        val newIsDelegated =
            state.whoProvidesDetailsTask.whoProvidesRentalDetailsStep.outcome
                ?.let { it == WhoProvidesRentalDetailsMode.LETTING_AGENT_PROVIDES } ?: return null
        return when {
            previousIsDelegatedToLettingAgent() == newIsDelegated -> WhoProvidesUpdateRouteMode.UNCHANGED
            newIsDelegated -> WhoProvidesUpdateRouteMode.CHANGED_TO_LETTING_AGENT
            else -> WhoProvidesUpdateRouteMode.CHANGED_TO_LANDLORD
        }
    }
}

@JourneyFrameworkComponent
class WhoProvidesUpdateRoutingStep(
    stepConfig: WhoProvidesUpdateRoutingStepConfig,
) : InternalStep<WhoProvidesUpdateRouteMode, PropertyRegistrationJourneyState>(stepConfig)
