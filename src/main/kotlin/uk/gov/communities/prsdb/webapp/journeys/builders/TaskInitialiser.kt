package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyComplete
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyExitStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.Task.Companion.configureSavable

class TaskInitialiser<TStateInit : JourneyState>(
    private val task: Task<TStateInit>,
    private val state: TStateInit,
    private val elementConfiguration: ElementConfiguration<SubjourneyComplete> =
        ElementConfiguration("Task ${task::class.simpleName}}"),
) : ConfigurableElement<SubjourneyComplete> by elementConfiguration,
    BuildableElement {
    private val conditionalConfigurations: MutableList<ConditionalElementConfiguration> = mutableListOf()

    private var taskRoute: String? = null

    fun routeSegment(segment: String): TaskInitialiser<TStateInit> {
        taskRoute = segment
        return this
    }

    override fun build(): List<JourneyStep<*, *, *>> {
        val nonNullDestinationProvider =
            elementConfiguration.nextDestinationProvider
                ?: throw JourneyInitialisationException("$initialiserName does not have a nextDestination defined")

        exitStepOverride?.let { task.setCustomExitStep(it) }

        val taskSubJourney =
            task.getTaskSubJourneyBuilder(state) {
                nextDestination(nonNullDestinationProvider)
            }

        taskSubJourney.configure {
            elementConfiguration.unreachableStepDestination?.let { unreachableStepDestinationIfNotSet(it) }
            elementConfiguration.additionalContentProviders.forEach { contentValueProvider ->
                withAdditionalContentProperties(contentValueProvider)
            }
        }
        taskSubJourney.configureFirst {
            elementConfiguration.backDestinationOverride?.let { backDestination(it) }
            parents(
                elementConfiguration.parentageProvider
                    ?: throw JourneyInitialisationException("$initialiserName does not have parentage defined"),
            )
        }
        if (elementConfiguration.shouldSaveProgress) {
            taskSubJourney.configureSavable {
                saveProgress()
            }
        }

        conditionalConfigurations.forEach { conditionConfig ->
            taskSubJourney.conditionallyConfigure(conditionConfig.condition, conditionConfig.configuration)
        }

        val builtSteps = taskSubJourney.build()

        // Prefix every requestable step in this task with the task route, so its URL path becomes
        // "<taskRoute>/<routeSegment>" (internal steps have no URL). Prepending rather than overwriting lets
        // nested routed tasks compose to "<outer>/<inner>/<routeSegment>", as inner tasks build first.
        taskRoute?.let { route ->
            builtSteps.filterIsInstance<JourneyStep.RequestableStep<*, *, *>>().forEach { step ->
                val existingPrefix = step.stepConfig.urlPathPrefix
                step.stepConfig.urlPathPrefix = if (existingPrefix != null) "$route/$existingPrefix" else route
            }
        }

        return builtSteps
    }

    override fun configure(configuration: ConfigurableElement<*>.() -> Unit) = configuration()

    override fun configureFirst(configuration: ConfigurableElement<*>.() -> Unit) = configuration()

    override fun conditionallyConfigure(
        condition: ConfigurableElement<*>.() -> Boolean,
        configuration: ConfigurableElement<*>.() -> Unit,
    ) {
        if (condition()) {
            configuration()
        }
        conditionalConfigurations.add(ConditionalElementConfiguration(condition, configuration))
    }

    var exitStepOverride: SubjourneyExitStep? = null
        private set

    fun customExitStep(step: SubjourneyExitStep) {
        if (exitStepOverride != null) {
            throw JourneyInitialisationException("Exit step has already been initialised")
        }
        exitStepOverride = step
    }
}
