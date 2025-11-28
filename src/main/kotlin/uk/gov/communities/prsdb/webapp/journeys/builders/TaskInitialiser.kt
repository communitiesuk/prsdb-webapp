package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationComplete
import uk.gov.communities.prsdb.webapp.journeys.Task

class TaskInitialiser<TStateInit : JourneyState>(
    private val task: Task<TStateInit>,
    private val state: TStateInit,
) : StepLikeInitialiser<NavigationComplete>(),
    BuildableElement {
    override val initialiserName: String = "Task ${this::class.simpleName ?: this::class.qualifiedName}}"

    private var allStepsConfiguration: MutableList<StepInitialiser<*, *, *>.() -> Unit> = mutableListOf()
    private var allElementsConfiguration: MutableList<StepLikeInitialiser<*>.() -> Unit> = mutableListOf()

    fun withConfigurationForAllSteps(configuration: StepInitialiser<*, *, *>.() -> Unit): TaskInitialiser<TStateInit> {
        allStepsConfiguration.add(configuration)
        return this
    }

    override fun build(): List<JourneyStep<*, *, *>> {
        val nonNullDestinationProvider =
            nextDestinationProvider ?: throw JourneyInitialisationException("$initialiserName does not have a nextDestination defined")
        val taskParentage =
            parentageProvider?.invoke() ?: throw JourneyInitialisationException("$initialiserName does not have parentage defined")

        val taskSubJourney =
            task.getTaskSubJourneyBuilder(state, taskParentage) {
                nextDestination(nonNullDestinationProvider)
            }

        taskSubJourney.configureSteps {
            unreachableStepDestination?.let { unreachableStepDestinationIfNotSet(it) }
            allStepsConfiguration.forEach { config ->
                config()
            }
        }
        taskSubJourney.configureElements {
            allElementsConfiguration.forEach { config ->
                config()
            }
        }

        return taskSubJourney.build()
    }

    override fun configureSteps(configuration: StepInitialiser<*, *, *>.() -> Unit) {
        allStepsConfiguration.add(configuration)
    }

    override fun configureElements(configuration: StepLikeInitialiser<*>.() -> Unit) {
        configuration()
        allElementsConfiguration.add(configuration)
    }
}
