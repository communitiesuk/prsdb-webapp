package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep

open class NavigationalStepConfig : AbstractGenericInternalStepConfig<NavigationComplete, JourneyState>() {
    override fun mode(state: JourneyState): NavigationComplete = NavigationComplete.COMPLETE
}

open class NavigationalStep(
    stepConfig: NavigationalStepConfig,
) : InternalStep<NavigationComplete, JourneyState>(stepConfig)

enum class NavigationComplete {
    COMPLETE,
}
