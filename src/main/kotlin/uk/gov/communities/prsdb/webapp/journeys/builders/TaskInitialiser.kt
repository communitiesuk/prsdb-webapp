package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.example.Destination
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete

class TaskInitialiser<TMode : Enum<TMode>, TStateInit : JourneyState>(
    private val task: Task<TMode, TStateInit>,
) {
    private val name: String
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
        val nonNullDestinationProvider =
            destinationProvider ?: throw JourneyInitialisationException("Task $name does not have a nextDestination defined")
        val taskParentage = parentage?.invoke() ?: throw JourneyInitialisationException("Task $name does not have parentage defined")

        return task.getTaskSteps(state, taskParentage) {
            nextDestination(nonNullDestinationProvider)
        }
    }
}
