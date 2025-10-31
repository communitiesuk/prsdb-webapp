package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiser
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.journeys.example.steps.NavigationalStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.NavigationalStepConfig

abstract class Task<TMode : Enum<TMode>, in TState : JourneyState> {
    fun getTaskSteps(
        state: TState,
        entryPoint: Parentage,
        exitInit: StepInitialiser<NavigationalStepConfig, TState, Complete>.() -> Unit,
    ): List<StepInitialiser<*, TState, *>> =
        makeSubJourney(state, entryPoint) +
            StepInitialiser<NavigationalStepConfig, TState, Complete>(null, notionalExitStep).apply {
                this.exitInit()
                this.parents { taskCompletionParentage(state) }
            }

    abstract fun makeSubJourney(
        state: TState,
        entryPoint: Parentage,
    ): List<StepInitialiser<*, TState, *>>

    abstract fun taskCompletionParentage(state: TState): Parentage

    val notionalExitStep: NavigationalStep<TState> = NavigationalStep(NavigationalStepConfig())
}
