package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiser
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.journeys.example.steps.NotionalStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.NotionalStepConfig

abstract class Task<TMode : Enum<TMode>, in TState : JourneyState> {
    fun getTaskSteps(
        exitRoute: String,
        state: TState,
        entryPoint: Parentage,
        exitInit: StepInitialiser<NotionalStepConfig, TState, Complete>.() -> Unit,
    ): List<StepInitialiser<*, TState, *>> =
        makeSubJourney(state, entryPoint) +
            StepInitialiser<NotionalStepConfig, TState, Complete>(exitRoute, notionalExitStep, true).apply {
                this.exitInit()
                this.parents { taskCompletionParentage(state) }
            }

    abstract fun makeSubJourney(
        state: TState,
        entryPoint: Parentage,
    ): List<StepInitialiser<*, TState, *>>

    abstract fun taskCompletionParentage(state: TState): Parentage

    val notionalExitStep: NotionalStep<TState> = NotionalStep(NotionalStepConfig())
}
