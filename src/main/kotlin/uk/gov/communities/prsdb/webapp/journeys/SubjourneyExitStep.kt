package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep

open class SubjourneyExitStepConfig : AbstractInternalStepConfig<SubjourneyComplete, JourneyState>() {
    override fun mode(state: JourneyState): SubjourneyComplete = SubjourneyComplete.COMPLETE
}

open class SubjourneyExitStep(
    stepConfig: SubjourneyExitStepConfig,
) : InternalStep<SubjourneyComplete, JourneyState>(stepConfig)

enum class SubjourneyComplete {
    COMPLETE,
}
