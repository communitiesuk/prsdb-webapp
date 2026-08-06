package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.DelegateKeyRegistry
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyComplete
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyExitStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.Task.Companion.configureSavable
import uk.gov.communities.prsdb.webapp.journeys.TaskRouteRedirectStep
import uk.gov.communities.prsdb.webapp.journeys.TaskRouteRedirectStepConfig

class TaskInitialiser<TStateInit : JourneyState, TDependencies : Any>(
    private val task: Task<TStateInit, TDependencies>,
    private val state: TStateInit,
    private val elementConfiguration: ElementConfiguration<SubjourneyComplete> =
        ElementConfiguration("Task ${task::class.simpleName}}"),
) : ConfigurableElement<SubjourneyComplete> by elementConfiguration,
    BuildableElement {
    private val conditionalConfigurations: MutableList<ConditionalElementConfiguration> = mutableListOf()

    // Per-step content/config supplied at the DSL call site (e.g. instance-specific field-set
    // headings), applied to the task's own named steps by identity before the sub-journey builds.
    private val stepConfigurations: MutableList<Pair<JourneyStep<*, *, *>, ConfigurableElement<*>.() -> Unit>> =
        mutableListOf()

    fun configureStep(
        step: JourneyStep<*, *, *>,
        configuration: ConfigurableElement<*>.() -> Unit,
    ) {
        stepConfigurations.add(step to configuration)
    }

    private var taskRoute: String? = null

    fun routeSegment(segment: String): TaskInitialiser<TStateInit, TDependencies> {
        taskRoute = segment
        return this
    }

    private var dependenciesProvider: (() -> TDependencies)? = null

    fun withDependencies(provider: () -> TDependencies) {
        if (dependenciesProvider != null) {
            throw JourneyInitialisationException("withDependencies has already been set")
        }
        dependenciesProvider = provider
    }

    override fun build(registry: DelegateKeyRegistry): List<JourneyStep<*, *, *>> {
        dependenciesProvider?.let { task.bindDependencies(it()) }
        if (task.requiresDependencies && !task.areDependenciesBound) {
            throw JourneyInitialisationException(
                "Task ${task::class.simpleName} requires dependencies but withDependencies { } was not called",
            )
        }

        task.bindRoute(taskRoute)

        // bindKeyRegistry must be called AFTER bindRoute, so the route-scoped keys are registered.
        task.bindKeyRegistry(registry)

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

        stepConfigurations.forEach { (step, configuration) ->
            taskSubJourney.configureStep(step, configuration)
        }

        val builtSteps = taskSubJourney.build(registry)

        // Prefix every requestable step in this task with the task route, so its URL path becomes
        // "<taskRoute>/<routeSegment>" (internal steps have no URL). Nested tasks will subsequently be
        // prefixed themselves and become "<outerTask>/<innerTask>/<routeSegment>"
        val stepsWithLanding =
            taskRoute?.let { route ->
                builtSteps.filterIsInstance<JourneyStep.RequestableStep<*, *, *>>().forEach { step ->
                    val existingPrefix = step.stepConfig.urlPathPrefix
                    step.stepConfig.urlPathPrefix = if (existingPrefix != null) "$route/$existingPrefix" else route
                }
                builtSteps + createLandingStep(route)
            } ?: builtSteps

        return stepsWithLanding
    }

    private fun createLandingStep(route: String): TaskRouteRedirectStep {
        val landingStep = TaskRouteRedirectStep(TaskRouteRedirectStepConfig())
        landingStep.initialize(
            segment = route,
            state = state,
            backDestinationOverride = null,
            redirectDestinationProvider = { Destination(task.firstStep) },
            parentage = NoParents(),
            unreachableStepDestinationProvider = { Destination(task.firstStep) },
            shouldSaveOnCompletion = false,
        )
        return landingStep
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
