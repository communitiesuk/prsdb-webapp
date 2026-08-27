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
    private lateinit var previouslyDelegated: () -> Boolean

    fun usingPreviouslyDelegated(previouslyDelegated: () -> Boolean): WhoProvidesUpdateRoutingStepConfig {
        this.previouslyDelegated = previouslyDelegated
        return this
    }

    // In a CYA change journey the newly-submitted answer lives in the child journey, while the previous answer
    // remains in the base journey until the change is committed. We therefore read the previous answer from the
    // base journey's cached value to decide whether the answer has actually changed.
    fun getPreviouslyDelegatedFromBaseJourney(state: PropertyRegistrationJourneyState): Boolean {
        val baseState = state.getBaseJourneyState() as PropertyRegistrationJourneyState
        return baseState.cachedWhoProvidesRentalDetails == WhoProvidesRentalDetails.LETTING_AGENT
    }

    override fun isSubClassInitialised() = ::previouslyDelegated.isInitialized

    override fun mode(state: PropertyRegistrationJourneyState): WhoProvidesUpdateRouteMode? {
        val newIsDelegated =
            state.whoProvidesDetailsTask.whoProvidesRentalDetailsStep.outcome
                ?.let { it == WhoProvidesRentalDetailsMode.LETTING_AGENT_PROVIDES } ?: return null
        return when {
            previouslyDelegated() == newIsDelegated -> WhoProvidesUpdateRouteMode.UNCHANGED
            newIsDelegated -> WhoProvidesUpdateRouteMode.CHANGED_TO_LETTING_AGENT
            else -> WhoProvidesUpdateRouteMode.CHANGED_TO_LANDLORD
        }
    }
}

@JourneyFrameworkComponent
class WhoProvidesUpdateRoutingStep(
    stepConfig: WhoProvidesUpdateRoutingStepConfig,
) : InternalStep<WhoProvidesUpdateRouteMode, PropertyRegistrationJourneyState>(stepConfig)
