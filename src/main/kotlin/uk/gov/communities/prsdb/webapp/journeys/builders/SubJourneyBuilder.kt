package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationComplete
import uk.gov.communities.prsdb.webapp.journeys.NavigationalStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Task

interface BuildableElement {
    fun build(): List<JourneyStep<*, *, *>>

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

    private var unreachableStepDestination: (() -> Destination)? = null

    private var additionalConfiguration: MutableList<ConditionalElementConfiguration> = mutableListOf()
    private var additionalFirstElementConfiguration: MutableList<ConfigurableElement<*>.() -> Unit> = mutableListOf()

    override fun build() = journeyElements.flatMap { element -> element.configureAndBuild() }

    protected fun BuildableElement.configureAndBuild(): List<JourneyStep<*, *, *>> {
        configure {
            unreachableStepDestination?.let { fallback -> unreachableStepDestinationIfNotSet(fallback) }
        }

        additionalConfiguration.forEach { this.conditionallyConfigure(it.condition, it.configuration) }

        return build()
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

    override fun conditionallyConfigure(
        condition: ConfigurableElement<*>.() -> Boolean,
        configuration: ConfigurableElement<*>.() -> Unit,
    ) {
        additionalConfiguration.add(ConditionalElementConfiguration(condition, configuration))
    }

    fun unreachableStepUrl(getUrl: () -> String) {
        if (unreachableStepDestination != null) {
            throw JourneyInitialisationException("unreachableStepDestination has already been set")
        }
        unreachableStepDestination = { Destination.ExternalUrl(getUrl()) }
    }

    fun unreachableStepStep(getStep: () -> JourneyStep<*, *, *>) {
        if (unreachableStepDestination != null) {
            throw JourneyInitialisationException("unreachableStepDestination has already been set")
        }
        unreachableStepDestination = { Destination(getStep()) }
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
    exitStepOverride: NavigationalStep? = null,
) : AbstractJourneyBuilder<TState>(journey) {
    var exitInits: MutableList<StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>.() -> Unit> = mutableListOf()
        private set

    val exitStep = exitStepOverride ?: NavigationalStep(NavigationalStepConfig())

    lateinit var firstStep: JourneyStep<*, *, *>
        private set

    override fun build(): List<JourneyStep<*, *, *>> {
        step<NavigationComplete, NavigationalStepConfig>(exitStep) {
            exitInits.forEach { it() }
        }
        val built = super.build()
        firstStep = built.first()
        return built
    }

    fun exitStep(init: StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>.() -> Unit) {
        exitInits.add(init)
    }
}
