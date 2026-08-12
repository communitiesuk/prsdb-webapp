package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.DelegateKeyRegistry
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyComplete
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyExitStep
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyExitStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Task

interface BuildableElement {
    fun build(registry: DelegateKeyRegistry = DelegateKeyRegistry()): List<JourneyStep<*, *, *>>

    fun configure(configuration: ConfigurableElement<*>.() -> Unit)

    fun configureFirst(configuration: ConfigurableElement<*>.() -> Unit)

    fun conditionallyConfigure(
        condition: ConfigurableElement<*>.() -> Boolean,
        configuration: ConfigurableElement<*>.() -> Unit,
    )
}

abstract class AbstractJourneyBuilder<TInternalState : JourneyState, TJourneyState : JourneyState>(
    private val privateJourney: TInternalState,
) : BuildableElement,
    JourneyBuilderDsl<TInternalState> {
    abstract val journey: TJourneyState

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

    private val additionalElements: MutableList<BuildableElement> = mutableListOf()
    protected val ownedElements get() = journeyElements + additionalElements

    override fun configure(configuration: ConfigurableElement<*>.() -> Unit) {
        additionalConfiguration.add(
            ConditionalElementConfiguration(
                { ownedElements.any { this === it } },
                configuration,
            ),
        )
    }

    override fun configureFirst(configuration: ConfigurableElement<*>.() -> Unit) {
        val firstElement = journeyElements.firstOrNull()
        if (firstElement == null) {
            additionalFirstElementConfiguration.add(configuration)
        } else {
            firstElement.configureFirst(configuration)
        }
    }

    override fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TInternalState>> step(
        uninitialisedStep: JourneyStep<TMode, *, TInternalState>,
        init: StepInitialiser<TStep, TInternalState, TMode>.() -> Unit,
    ) {
        val stepInitialiser = StepInitialiser<TStep, TInternalState, TMode>(uninitialisedStep, privateJourney)
        stepInitialiser.init()
        if (journeyElements.isEmpty()) {
            stepInitialiser.configureFirst {
                additionalFirstElementConfiguration.forEach { it() }
            }
        }
        journeyElements.add(stepInitialiser)
    }

    override fun <TTaskState : JourneyState, TDependencies : Any> task(
        uninitialisedTask: Task<TTaskState, TDependencies>,
        routeSegment: String?,
        init: TaskInitialiser<TTaskState, TDependencies>.() -> Unit,
    ) {
        val taskInitialiser = TaskInitialiser(uninitialisedTask, uninitialisedTask.taskState)
        routeSegment?.let { taskInitialiser.routeSegment(it) }
        taskInitialiser.init()
        if (journeyElements.isEmpty()) {
            taskInitialiser.configureFirst {
                additionalFirstElementConfiguration.forEach { it() }
            }
        }
        journeyElements.add(taskInitialiser)
    }

    fun <TEmbeddedState : JourneyState> fromTask(
        task: TEmbeddedState,
        init: EmbedBuilder<TEmbeddedState, TInternalState>.() -> Unit,
    ) {
        val builder = EmbedBuilder(task, privateJourney)
        builder.init()
        registerTransparentBuilder(builder)
    }

    fun <TEmbeddedState, TDependencies : Any> fromTask(
        task: TEmbeddedState,
        dependencies: TDependencies,
        init: EmbedBuilder<TEmbeddedState, TInternalState>.() -> Unit,
    ) where TEmbeddedState : JourneyState, TEmbeddedState : Task<*, TDependencies> {
        val builder = EmbedBuilder(task, privateJourney)
        task.bindDependencies(dependencies)
        builder.init()
        registerTransparentBuilder(builder)
    }

    protected fun registerTransparentBuilder(builder: AbstractJourneyBuilder<*, *>) {
        if (journeyElements.isEmpty()) {
            builder.configureFirst { additionalFirstElementConfiguration.forEach { it() } }
        }
        journeyElements.add(builder)
        additionalElements.addAll(builder.ownedElements)
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
    override val journey: TState,
    exitStepOverride: SubjourneyExitStep? = null,
) : AbstractJourneyBuilder<TState, TState>(journey) {
    var exitInits: MutableList<StepInitialiser<SubjourneyExitStepConfig, TState, SubjourneyComplete>.() -> Unit> =
        mutableListOf()
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
