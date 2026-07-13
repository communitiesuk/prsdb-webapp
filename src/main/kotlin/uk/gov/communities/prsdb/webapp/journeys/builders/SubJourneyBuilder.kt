package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.DelegateKeyRegistry
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.SelfStatedRoutableTask
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyComplete
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyExitStep
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyExitStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Task

interface BuildableElement {
    // A single DelegateKeyRegistry is threaded through the whole build so the journey state and every task register
    // their route-scoped delegate keys into it, letting cross-element key collisions be detected at build time.
    fun build(registry: DelegateKeyRegistry = DelegateKeyRegistry()): List<JourneyStep<*, *, *>>

    fun configure(configuration: ConfigurableElement<*>.() -> Unit)

    fun configureFirst(configuration: ConfigurableElement<*>.() -> Unit)

    fun conditionallyConfigure(
        condition: ConfigurableElement<*>.() -> Boolean,
        configuration: ConfigurableElement<*>.() -> Unit,
    )
}

abstract class AbstractJourneyBuilder<TState : JourneyState>(
    val journey: TState,
) : BuildableElement,
    JourneyBuilderDsl<TState> {
    private val journeyElements: MutableList<BuildableElement> = mutableListOf()

    private var defaultUnreachableStepDestination: (() -> Destination)? = null

    private var additionalConfiguration: MutableList<ConditionalElementConfiguration> = mutableListOf()
    private var additionalFirstElementConfiguration: MutableList<ConfigurableElement<*>.() -> Unit> = mutableListOf()

    override fun build(registry: DelegateKeyRegistry) = journeyElements.flatMap { element -> element.configureAndBuild(registry) }

    protected fun BuildableElement.configureAndBuild(registry: DelegateKeyRegistry): List<JourneyStep<*, *, *>> {
        configure {
            defaultUnreachableStepDestination?.let { fallback -> unreachableStepDestinationIfNotSet(fallback) }
        }

        additionalConfiguration.forEach { this.conditionallyConfigure(it.condition, it.configuration) }

        return build(registry)
    }

    override fun configure(configuration: ConfigurableElement<*>.() -> Unit) {
        additionalConfiguration.add(ConditionalElementConfiguration({ journeyElements.any { this === it } }, configuration))
    }

    override fun configureFirst(configuration: ConfigurableElement<*>.() -> Unit) {
        val firstElement = journeyElements.firstOrNull()
        if (firstElement == null) {
            additionalFirstElementConfiguration.add(configuration)
        } else {
            firstElement.configureFirst(configuration)
        }
    }

    override fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> step(
        uninitialisedStep: JourneyStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        val stepInitialiser = StepInitialiser<TStep, TState, TMode>(uninitialisedStep, journey)
        stepInitialiser.init()
        if (journeyElements.isEmpty()) {
            stepInitialiser.configureFirst {
                additionalFirstElementConfiguration.forEach { it() }
            }
        }
        journeyElements.add(stepInitialiser)
    }

    override fun task(
        uninitialisedTask: Task<TState>,
        init: TaskInitialiser<TState>.() -> Unit,
    ) {
        val taskInitialiser = TaskInitialiser(uninitialisedTask, journey)
        taskInitialiser.init()
        journeyElements.add(taskInitialiser)
    }

    override fun <TTaskState : JourneyState> routableTask(
        uninitialisedTask: SelfStatedRoutableTask<TTaskState>,
        routeSegment: String?,
        init: TaskInitialiser<TTaskState>.() -> Unit,
    ) {
        // The task IS its own state, so build its sub-journey against the task itself. A non-null `routeSegment`
        // prefixes each step's URL, giving "<routeSegment>/<step>", and (via the task's own route-scoped data
        // keys, applied by TaskInitialiser.build -> task.bindRoute) its stored data; null keeps them bare. No
        // external state delegate is needed - the task sources JourneyState from its own journeyStateService.
        val taskInitialiser = TaskInitialiser(uninitialisedTask, uninitialisedTask.taskState)
        routeSegment?.let { taskInitialiser.routeSegment(it) }
        taskInitialiser.init()
        journeyElements.add(taskInitialiser)
    }

    override fun conditionallyConfigure(
        condition: ConfigurableElement<*>.() -> Boolean,
        configuration: ConfigurableElement<*>.() -> Unit,
    ) {
        additionalConfiguration.add(ConditionalElementConfiguration(condition, configuration))
    }

    fun unreachableStepUrl(getUrl: () -> String) {
        if (defaultUnreachableStepDestination != null) {
            throw JourneyInitialisationException("unreachableStepDestination has already been set")
        }
        defaultUnreachableStepDestination = { Destination.ExternalUrl(getUrl()) }
    }

    fun unreachableStepStep(getStep: () -> JourneyStep<*, *, *>) {
        if (defaultUnreachableStepDestination != null) {
            throw JourneyInitialisationException("unreachableStepDestination has already been set")
        }
        defaultUnreachableStepDestination = { Destination(getStep()) }
    }

    fun unreachableStepDestination(getDestination: () -> Destination) {
        if (defaultUnreachableStepDestination != null) {
            throw JourneyInitialisationException("unreachableStepDestination has already been set")
        }
        defaultUnreachableStepDestination = getDestination
    }

    fun configureTagged(
        tag: String,
        configuration: ConfigurableElement<*>.() -> Unit,
    ) {
        additionalConfiguration.add(ConditionalElementConfiguration({ tags.contains(tag) }, configuration))
    }

    fun configureStep(
        step: JourneyStep<*, *, *>,
        configuration: ConfigurableElement<*>.() -> Unit,
    ) {
        additionalConfiguration.add(
            ConditionalElementConfiguration(
                { this is StepInitialiser<*, *, *> && isForStep(step) },
                configuration,
            ),
        )
    }
}

open class SubJourneyBuilder<TState : JourneyState>(
    journey: TState,
    exitStepOverride: SubjourneyExitStep? = null,
) : AbstractJourneyBuilder<TState>(journey) {
    var exitInits: MutableList<StepInitialiser<SubjourneyExitStepConfig, TState, SubjourneyComplete>.() -> Unit> = mutableListOf()
        private set

    val exitStep = exitStepOverride ?: SubjourneyExitStep(SubjourneyExitStepConfig())

    lateinit var firstStep: JourneyStep<*, *, *>
        private set

    override fun build(registry: DelegateKeyRegistry): List<JourneyStep<*, *, *>> {
        step<SubjourneyComplete, SubjourneyExitStepConfig>(exitStep) {
            exitInits.forEach { it() }
        }
        val built = super.build(registry)
        firstStep = built.first()
        return built
    }

    fun exitStep(init: StepInitialiser<SubjourneyExitStepConfig, TState, SubjourneyComplete>.() -> Unit) {
        exitInits.add(init)
    }

    var taskStatusOverride: (() -> TaskStatus)? = null
        private set

    fun taskStatus(provider: () -> TaskStatus) {
        if (taskStatusOverride != null) {
            throw JourneyInitialisationException("Task status override has already been set")
        }
        taskStatusOverride = provider
    }
}
