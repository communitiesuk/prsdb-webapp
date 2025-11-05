package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiser

abstract class Task<TMode : Enum<TMode>, in TState : JourneyState> {
    fun getTaskSteps(
        state: TState,
        entryPoint: Parentage,
        exitInit: StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>.() -> Unit,
    ): List<StepInitialiser<*, TState, *>> =
        makeSubJourney(state, entryPoint) +
            StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>(null, notionalExitStep).apply {
                this.exitInit()
                this.parents { taskCompletionParentage(state) }
            }

    abstract fun makeSubJourney(
        state: TState,
        entryPoint: Parentage,
    ): List<StepInitialiser<*, TState, *>>

    abstract fun taskCompletionParentage(state: TState): Parentage

    fun taskStatus(state: TState): TaskStatus =
        when {
            notionalExitStep.isStepReachable -> TaskStatus.COMPLETED
            firstStepInTask(state).outcome() != null -> TaskStatus.IN_PROGRESS
            firstStepInTask(state).isStepReachable -> TaskStatus.NOT_STARTED
            else -> TaskStatus.CANNOT_START
        }

    val notionalExitStep: NavigationalStep = NavigationalStep(NavigationalStepConfig())

    abstract fun firstStepInTask(state: TState): JourneyStep<*, *, TState>
}
