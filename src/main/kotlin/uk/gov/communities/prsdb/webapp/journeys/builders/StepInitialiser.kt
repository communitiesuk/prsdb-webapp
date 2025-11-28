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

abstract class StepLikeInitialiser<TMode : Enum<TMode>> {
    abstract val initialiserName: String
    protected var nextDestinationProvider: ((mode: TMode) -> Destination)? = null
    protected var parentageProvider: (() -> Parentage)? = null
    protected var unreachableStepDestination: (() -> Destination)? = null

    fun nextStep(nextStepProvider: (mode: TMode) -> JourneyStep<*, *, *>): StepLikeInitialiser<TMode> =
        nextDestination { mode -> Destination(nextStepProvider(mode)) }

    fun nextUrl(nextUrlProvider: (mode: TMode) -> String): StepLikeInitialiser<TMode> =
        nextDestination { mode -> Destination.ExternalUrl(nextUrlProvider(mode)) }

    fun nextDestination(destinationProvider: (mode: TMode) -> Destination): StepLikeInitialiser<TMode> {
        if (nextDestinationProvider != null) {
            throw JourneyInitialisationException("$initialiserName already has a next destination defined")
        }
        nextDestinationProvider = destinationProvider
        return this
    }

    fun noNextDestination(): StepLikeInitialiser<TMode> {
        if (nextDestinationProvider != null) {
            throw JourneyInitialisationException("$initialiserName already has a next destination defined")
        }
        nextDestinationProvider = { throw PrsdbWebException("$initialiserName has no next destination so cannot be posted to") }
        return this
    }

    fun parents(currentParentage: () -> Parentage): StepLikeInitialiser<TMode> {
        if (parentageProvider != null) {
            throw JourneyInitialisationException("$initialiserName already has parentage defined")
        }
        parentageProvider = currentParentage
        return this
    }

    fun initialStep(): StepLikeInitialiser<TMode> = parents { NoParents() }

    fun unreachableStepUrl(getDestination: () -> String): StepLikeInitialiser<TMode> {
        if (unreachableStepDestination != null) {
            throw JourneyInitialisationException("$initialiserName already has an unreachableStepDestination defined")
        }
        unreachableStepDestination = { Destination.ExternalUrl(getDestination()) }
        return this
    }

    fun unreachableStepDestinationIfNotSet(getDestination: () -> Destination): StepLikeInitialiser<TMode> {
        if (unreachableStepDestination != null) {
            return this
        }
        unreachableStepDestination = getDestination
        return this
    }
}

class StepInitialiser<TStep : AbstractStepConfig<TMode, *, TState>, in TState : JourneyState, TMode : Enum<TMode>>(
    val segment: String?,
    private val step: JourneyStep<TMode, *, TState>,
    private val state: TState,
) : StepLikeInitialiser<TMode>(),
    BuildableElement {
    init {
        if (step.initialisationStage != StepInitialisationStage.UNINITIALISED) {
            throw JourneyInitialisationException("${segment ?: step::class.simpleName} has already been initialised")
        }
    }

    override val initialiserName: String = "Step ${segment ?: step::class.simpleName}"

    private var backUrlOverride: (() -> String?)? = null
    private var additionalConfig: (TStep.() -> Unit)? = null
    private var additionalContentProviders: MutableList<() -> Pair<String, Any>> = mutableListOf()

    fun stepSpecificInitialisation(configure: TStep.() -> Unit): StepLikeInitialiser<TMode> {
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

    fun withAdditionalContentProperty(getAdditionalContent: () -> Pair<String, Any>): StepInitialiser<TStep, TState, TMode> {
        additionalContentProviders.add(getAdditionalContent)
        return this
    }

    override fun build(): List<JourneyStep<*, *, *>> = listOf(build(state))

    override fun configureSteps(configuration: StepInitialiser<*, *, *>.() -> Unit) {
        configuration()
    }

    override fun configureElements(configuration: StepLikeInitialiser<*>.() -> Unit) = configureSteps(configuration)

    private fun build(state: TState): JourneyStep<TMode, *, TState> {
        val parentage = parentageProvider?.invoke() ?: throw JourneyInitialisationException("$initialiserName has no parentage defined")
        checkForUninitialisedParents(parentage.potentialParents)

        step.initialize(
            segment,
            state,
            backUrlOverride,
            nextDestinationProvider ?: throw JourneyInitialisationException("$initialiserName has no nextDestination defined"),
            parentage,
            unreachableStepDestination
                ?: throw JourneyInitialisationException(
                    "$initialiserName has no unreachableStepDestination defined, and there is no default set at the journey level either",
                ),
        ) {
            additionalContentProviders.associate { provider -> provider() }
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
