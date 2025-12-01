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

    fun nextStep(nextStepProvider: (mode: TMode) -> JourneyStep<*, *, *>): ConfigurableElement<TMode>

    fun nextUrl(nextUrlProvider: (mode: TMode) -> String): ConfigurableElement<TMode>

    fun nextDestination(destinationProvider: (mode: TMode) -> Destination): ConfigurableElement<TMode>

    fun noNextDestination(): ConfigurableElement<TMode>

    fun parents(currentParentage: () -> Parentage): ConfigurableElement<TMode>

    fun initialStep(): ConfigurableElement<TMode>

    fun unreachableStepUrl(getDestination: () -> String): ConfigurableElement<TMode>

    fun unreachableStepDestinationIfNotSet(getDestination: () -> Destination): ConfigurableElement<TMode>

    fun withAdditionalContentProperty(getAdditionalContent: () -> Pair<String, Any>): ConfigurableElement<TMode>
}

class ElementConfiguration<TMode : Enum<TMode>>(
    override val initialiserName: String,
) : ConfigurableElement<TMode> {
    var nextDestinationProvider: ((mode: TMode) -> Destination)? = null
    var parentageProvider: (() -> Parentage)? = null
    var unreachableStepDestination: (() -> Destination)? = null
    var additionalContentProviders: MutableList<() -> Pair<String, Any>> = mutableListOf()

    override fun nextStep(nextStepProvider: (mode: TMode) -> JourneyStep<*, *, *>): ConfigurableElement<TMode> =
        nextDestination { mode -> Destination(nextStepProvider(mode)) }

    override fun nextUrl(nextUrlProvider: (mode: TMode) -> String): ConfigurableElement<TMode> =
        nextDestination { mode -> Destination.ExternalUrl(nextUrlProvider(mode)) }

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
}

class StepInitialiser<TStep : AbstractStepConfig<TMode, *, TState>, in TState : JourneyState, TMode : Enum<TMode>>(
    val segment: String?,
    private val step: JourneyStep<TMode, *, TState>,
    private val state: TState,
    val elementConfiguration: ElementConfiguration<TMode> = ElementConfiguration("Step ${segment ?: step::class.simpleName}"),
) : ConfigurableElement<TMode> by elementConfiguration,
    BuildableElement {
    init {
        if (step.initialisationStage != StepInitialisationStage.UNINITIALISED) {
            throw JourneyInitialisationException("${segment ?: step::class.simpleName} has already been initialised")
        }
    }

    private var backUrlOverride: (() -> String?)? = null
    private var additionalConfig: (TStep.() -> Unit)? = null

    fun stepSpecificInitialisation(configure: TStep.() -> Unit): StepInitialiser<TStep, TState, TMode> {
        if (additionalConfig != null) {
            throw JourneyInitialisationException("$initialiserName already has additional configuration defined")
        }
        additionalConfig = configure
        return this
    }

    fun backUrl(backUrlProvider: () -> String?): StepInitialiser<TStep, TState, TMode> {
        if (backUrlOverride != null) {
            throw JourneyInitialisationException("$initialiserName already has an explicit backUrl defined")
        }
        backUrlOverride = backUrlProvider
        return this
    }

    override fun build(): List<JourneyStep<*, *, *>> = listOf(build(state))

    override fun configure(configuration: ConfigurableElement<*>.() -> Unit) = configuration()

    private fun build(state: TState): JourneyStep<TMode, *, TState> {
        val parentage =
            elementConfiguration.parentageProvider?.invoke()
                ?: throw JourneyInitialisationException("$initialiserName has no parentage defined")
        checkForUninitialisedParents(parentage.potentialParents)

        step.initialize(
            segment,
            state,
            backUrlOverride,
            elementConfiguration.nextDestinationProvider
                ?: throw JourneyInitialisationException("$initialiserName has no nextDestination defined"),
            parentage,
            elementConfiguration.unreachableStepDestination
                ?: throw JourneyInitialisationException(
                    "$initialiserName has no unreachableStepDestination defined, and there is no default set at the journey level either",
                ),
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
