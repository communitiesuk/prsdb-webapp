package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationComplete
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.Task

class TaskInitialiser<TStateInit : JourneyState>(
    private val task: Task<TStateInit>,
    private val state: TStateInit,
) : BuildableElement {
    private val name: String
        get() = this::class.simpleName!!

    private var destinationProvider: ((mode: NavigationComplete) -> Destination)? = null
    private var parentage: (() -> Parentage)? = null

    private var allStepsConfiguration: MutableList<StepInitialiser<*, *, *>.() -> Unit> = mutableListOf()

    fun redirectToStep(nextStepProvider: (mode: NavigationComplete) -> JourneyStep<*, *, *>): TaskInitialiser<TStateInit> {
        if (destinationProvider != null) {
            throw JourneyInitialisationException("Task $name already has a redirectTo defined")
        }
        destinationProvider = { mode -> Destination(nextStepProvider(mode)) }
        return this
    }

    fun redirectToDestination(destination: (mode: NavigationComplete) -> Destination): TaskInitialiser<TStateInit> {
        if (destinationProvider != null) {
            throw JourneyInitialisationException("Task $name already has a redirectTo defined")
        }
        destinationProvider = destination
        return this
    }

    fun parents(currentParentage: () -> Parentage): TaskInitialiser<TStateInit> {
        if (parentage != null) {
            throw JourneyInitialisationException("Task $name already has parentage defined")
        }
        parentage = currentParentage
        return this
    }

    fun withConfigurationForAllSteps(configuration: StepInitialiser<*, *, *>.() -> Unit): TaskInitialiser<TStateInit> {
        allStepsConfiguration.add(configuration)
        return this
    }

    override fun build(): List<JourneyStep<*, *, *>> {
        val nonNullDestinationProvider =
            destinationProvider ?: throw JourneyInitialisationException("Task $name does not have a nextDestination defined")
        val taskParentage = parentage?.invoke() ?: throw JourneyInitialisationException("Task $name does not have parentage defined")

        val taskSubJourney =
            task.getTaskSubJourneyBuilder(state, taskParentage) {
                nextDestination(nonNullDestinationProvider)
            }

        taskSubJourney.configureSteps {
            allStepsConfiguration.forEach { config ->
                config()
            }
        }

        return taskSubJourney.build()
    }

    override fun configureSteps(configuration: StepInitialiser<*, *, *>.() -> Unit) {
        allStepsConfiguration.add(configuration)
    }
}
