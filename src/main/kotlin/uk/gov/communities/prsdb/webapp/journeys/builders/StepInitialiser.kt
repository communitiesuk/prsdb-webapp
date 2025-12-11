package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage

interface ConfigurableElement<TMode : Enum<TMode>> {
    val initialiserName: String
    val tags: Set<String>

    fun nextStep(nextStepProvider: (mode: TMode) -> JourneyStep<*, *, *>): ConfigurableElement<TMode>

    fun nextUrl(nextUrlProvider: (mode: TMode) -> String): ConfigurableElement<TMode>

    fun nextDestination(destinationProvider: (mode: TMode) -> Destination): ConfigurableElement<TMode>

    fun modifyNextDestination(modify: (original: (mode: TMode) -> Destination) -> (mode: TMode) -> Destination): ConfigurableElement<TMode>

    fun modifyNextDestination(merged: (mode: TMode, original: (mode: TMode) -> Destination) -> Destination): ConfigurableElement<TMode>

    fun noNextDestination(): ConfigurableElement<TMode>

    fun parents(currentParentage: () -> Parentage): ConfigurableElement<TMode>

    fun initialStep(): ConfigurableElement<TMode>

    fun unreachableStepUrl(getDestination: () -> String): ConfigurableElement<TMode>

    fun unreachableStepDestinationIfNotSet(getDestination: () -> Destination): ConfigurableElement<TMode>

    fun withAdditionalContentProperty(getAdditionalContent: () -> Pair<String, Any>): ConfigurableElement<TMode>

    fun taggedWith(vararg stepTags: String): ConfigurableElement<TMode>

    fun backUrl(backUrlProvider: () -> String?): ConfigurableElement<TMode>

    fun backStep(backStepProvider: () -> JourneyStep<*, *, *>?): ConfigurableElement<TMode>

    fun backDestination(backUrlProvider: () -> Destination): ConfigurableElement<TMode>

    fun saveProgress(): ConfigurableElement<TMode>
}

class ElementConfiguration<TMode : Enum<TMode>>(
    override var initialiserName: String,
) : ConfigurableElement<TMode> {
    var nextDestinationProvider: ((mode: TMode) -> Destination)? = null
    var parentageProvider: (() -> Parentage)? = null
    var unreachableStepDestination: (() -> Destination)? = null
    var additionalContentProviders: MutableList<() -> Pair<String, Any>> = mutableListOf()
    var backDestinationOverride: (() -> Destination)? = null
    var shouldSaveProgress: Boolean = false
    override var tags: Set<String> = emptySet()

    override fun nextStep(nextStepProvider: (mode: TMode) -> JourneyStep<*, *, *>): ConfigurableElement<TMode> =
        nextDestination { mode -> Destination(nextStepProvider(mode)) }

    override fun nextUrl(nextUrlProvider: (mode: TMode) -> String): ConfigurableElement<TMode> =
        nextDestination { mode -> Destination.ExternalUrl(nextUrlProvider(mode)) }

    override fun modifyNextDestination(
        modify: (original: (mode: TMode) -> Destination) -> (mode: TMode) -> Destination,
    ): ConfigurableElement<TMode> {
        val originalProvider =
            nextDestinationProvider
                ?: throw JourneyInitialisationException("$initialiserName has no nextDestination defined, so cannot be modified")
        nextDestinationProvider = { mode -> modify(originalProvider)(mode) }
        return this
    }

    override fun modifyNextDestination(
        merged: (mode: TMode, original: (mode: TMode) -> Destination) -> Destination,
    ): ConfigurableElement<TMode> {
        val originalProvider =
            nextDestinationProvider
                ?: throw JourneyInitialisationException("$initialiserName has no nextDestination defined, so cannot be modified")
        nextDestinationProvider = { mode -> merged(mode, originalProvider) }
        return this
    }

    override fun nextDestination(destinationProvider: (mode: TMode) -> Destination): ConfigurableElement<TMode> {
        if (nextDestinationProvider != null) {
            throw JourneyInitialisationException("$initialiserName already has a next destination defined")
        }
        nextDestinationProvider = destinationProvider
        return this
    }

    override fun noNextDestination(): ConfigurableElement<TMode> {
        if (nextDestinationProvider != null) {
            throw JourneyInitialisationException("$initialiserName already has a next destination defined")
        }
        nextDestinationProvider = { throw PrsdbWebException("$initialiserName has no next destination so cannot be posted to") }
        return this
    }

    override fun backStep(backStepProvider: () -> JourneyStep<*, *, *>?): ConfigurableElement<TMode> =
        backDestination { Destination(backStepProvider()) }

    override fun backDestination(backUrlProvider: () -> Destination): ConfigurableElement<TMode> {
        if (backDestinationOverride != null) {
            throw JourneyInitialisationException("$initialiserName already has an explicit backUrl defined")
        }
        backDestinationOverride = backUrlProvider
        return this
    }

    override fun saveProgress(): ConfigurableElement<TMode> {
        shouldSaveProgress = true
        return this
    }

    override fun backUrl(backUrlProvider: () -> String?): ConfigurableElement<TMode> =
        backDestination { backUrlProvider()?.let { Destination.ExternalUrl(it) } ?: Destination.Nowhere() }

    override fun parents(currentParentage: () -> Parentage): ConfigurableElement<TMode> {
        if (parentageProvider != null) {
            throw JourneyInitialisationException("$initialiserName already has parentage defined")
        }
        parentageProvider = currentParentage
        return this
    }

    override fun initialStep(): ConfigurableElement<TMode> = parents { NoParents() }

    override fun unreachableStepUrl(getDestination: () -> String): ConfigurableElement<TMode> {
        if (unreachableStepDestination != null) {
            throw JourneyInitialisationException("$initialiserName already has an unreachableStepDestination defined")
        }
        unreachableStepDestination = { Destination.ExternalUrl(getDestination()) }
        return this
    }

    override fun unreachableStepDestinationIfNotSet(getDestination: () -> Destination): ConfigurableElement<TMode> {
        if (unreachableStepDestination != null) {
            return this
        }
        unreachableStepDestination = getDestination
        return this
    }

    override fun withAdditionalContentProperty(getAdditionalContent: () -> Pair<String, Any>): ConfigurableElement<TMode> {
        additionalContentProviders.add(getAdditionalContent)
        return this
    }

    override fun taggedWith(vararg stepTags: String): ConfigurableElement<TMode> {
        tags = tags + stepTags
        return this
    }
}

class StepInitialiser<TStep : AbstractStepConfig<TMode, *, TState>, in TState : JourneyState, TMode : Enum<TMode>>(
    private val step: JourneyStep<TMode, *, TState>,
    private val state: TState,
    private val elementConfiguration: ElementConfiguration<TMode> = ElementConfiguration("Step ${step::class.simpleName}"),
) : ConfigurableElement<TMode> by elementConfiguration,
    BuildableElement {
    init {
        if (step.initialisationStage != StepInitialisationStage.UNINITIALISED) {
            throw JourneyInitialisationException("$initialiserName has already been initialised")
        }
    }

    private var additionalConfig: (TStep.() -> Unit)? = null
    private var segment: String? = null

    fun routeSegment(segment: String): StepInitialiser<TStep, TState, TMode> {
        this.segment = segment
        elementConfiguration.initialiserName = "Step $segment (${step::class.simpleName})"
        return this
    }

    fun stepSpecificInitialisation(configure: TStep.() -> Unit): StepInitialiser<TStep, TState, TMode> {
        if (additionalConfig != null) {
            throw JourneyInitialisationException("$initialiserName already has additional configuration defined")
        }
        additionalConfig = configure
        return this
    }

    override fun build(): List<JourneyStep<*, *, *>> = listOf(build(state))

    override fun configure(configuration: ConfigurableElement<*>.() -> Unit) = configuration()

    override fun configureFirst(configuration: ConfigurableElement<*>.() -> Unit) = configure(configuration)

    private fun build(state: TState): JourneyStep<TMode, *, TState> {
        val parentage =
            elementConfiguration.parentageProvider?.invoke()
                ?: throw JourneyInitialisationException("$initialiserName has no parentage defined")
        checkForUninitialisedParents(parentage.potentialParents)

        step.initialize(
            segment,
            state,
            elementConfiguration.backDestinationOverride,
            elementConfiguration.nextDestinationProvider
                ?: throw JourneyInitialisationException("$initialiserName has no nextDestination defined"),
            parentage,
            elementConfiguration.unreachableStepDestination
                ?: throw JourneyInitialisationException(
                    "$initialiserName has no unreachableStepDestination defined, and there is no default set at the journey level either",
                ),
            elementConfiguration.shouldSaveProgress,
        ) {
            elementConfiguration.additionalContentProviders.associate { provider -> provider() }
        }

        if (step.initialisationStage == StepInitialisationStage.UNINITIALISED) {
            throw JourneyInitialisationException("$initialiserName base class has not been initialised correctly")
        }

        // TODO PRSD-1546: Fix generic typing so this cast is not required
        additionalConfig?.let { configure -> (step.stepConfig as? TStep)?.configure() }
        if (step.initialisationStage != StepInitialisationStage.FULLY_INITIALISED) {
            throw JourneyInitialisationException("Custom configuration for $initialiserName has not fully initialised the step")
        }

        return step
    }

    private fun checkForUninitialisedParents(potentialParents: List<JourneyStep<*, *, *>>) {
        val uninitialisedParents =
            potentialParents.filter {
                it.initialisationStage != StepInitialisationStage.FULLY_INITIALISED
            }

        if (uninitialisedParents.any()) {
            val parentNames = uninitialisedParents.joinToString { "\n- $it" }
            throw JourneyInitialisationException(
                "$initialiserName has uninitialised potential parents on initialisation: $parentNames\n" +
                    "This could imply a dependency loop, or that these two steps are declared in the wrong order.",
            )
        }
    }
}
