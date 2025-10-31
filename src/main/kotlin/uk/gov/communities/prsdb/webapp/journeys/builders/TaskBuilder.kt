package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.example.Destination
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete

class TaskBuilder<TState : JourneyState, TMode : Enum<TMode>>(
    val task: TState,
) {
    private val stepsUnderConstruction: MutableList<StepInitialiser<*, TState, *>> = mutableListOf()

    fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> step(
        segment: String,
        uninitialisedStep: JourneyStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        val stepInitialiser = StepInitialiser<TStep, TState, TMode>(segment, uninitialisedStep)
        stepInitialiser.init()
        stepsUnderConstruction.add(stepInitialiser)
    }

    companion object {
        fun <TState : JourneyState, TMode : Enum<TMode>> subJourney(
            state: TState,
            init: TaskBuilder<TState, TMode>.() -> Unit = {},
        ): List<StepInitialiser<*, TState, *>> {
            val builder = TaskBuilder<TState, TMode>(state)
            builder.init()
            return builder.stepsUnderConstruction
        }
    }
}

class TaskInitialiser<TMode : Enum<TMode>, TStateInit : JourneyState>(
    private val task: Task<TMode, TStateInit>,
) {
    val name: String
        get() = this::class.simpleName!!

    private var destinationProvider: ((mode: Complete) -> Destination)? = null
    private var parentage: (() -> Parentage)? = null

    fun redirectToStep(nextStepProvider: (mode: Complete) -> JourneyStep<*, *, TStateInit>): TaskInitialiser<TMode, TStateInit> {
        if (destinationProvider != null) {
            throw JourneyInitialisationException("Task $name already has a redirectTo defined")
        }
        destinationProvider = { mode -> Destination(nextStepProvider(mode)) }
        return this
    }

    fun redirectToDestination(destination: (mode: Complete) -> Destination): TaskInitialiser<TMode, TStateInit> {
        if (destinationProvider != null) {
            throw JourneyInitialisationException("Task $name already has a redirectTo defined")
        }
        destinationProvider = destination
        return this
    }

    fun parents(currentParentage: () -> Parentage): TaskInitialiser<TMode, TStateInit> {
        if (parentage != null) {
            throw JourneyInitialisationException("Task $name already has parentage defined")
        }
        parentage = currentParentage
        return this
    }

    fun mapToStepInitialisers(state: TStateInit): List<StepInitialiser<*, TStateInit, *>> {
        if (destinationProvider == null) {
            throw JourneyInitialisationException("Task $name does not have a redirectTo defined")
        }
        if (parentage == null) {
            throw JourneyInitialisationException("Task $name does not have parentage defined")
        }

        with(destinationProvider!!) {
            return task.getTaskSteps(state, parentage!!.invoke()) {
                nextDestination(this@with)
            }
        }
    }
}
