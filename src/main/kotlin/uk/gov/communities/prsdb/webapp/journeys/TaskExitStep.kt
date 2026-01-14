package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep

open class TaskExitStepConfig : AbstractGenericInternalStepConfig<TaskComplete, JourneyState>() {
    override fun mode(state: JourneyState): TaskComplete = TaskComplete.COMPLETE
}

open class TaskExitStep(
    stepConfig: TaskExitStepConfig,
) : InternalStep<TaskComplete, JourneyState>(stepConfig)

enum class TaskComplete {
    COMPLETE,
}
