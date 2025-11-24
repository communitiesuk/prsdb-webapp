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

class StepInitialiser<TStep : AbstractStepConfig<TMode, *, TState>, in TState : JourneyState, TMode : Enum<TMode>>(
    val segment: String?,
    private val step: JourneyStep<TMode, *, TState>,
    private val state: TState,
) : StepCollectionBuilder {
    init {
        if (step.initialisationStage != StepInitialisationStage.UNINITIALISED) {
            throw JourneyInitialisationException("Step $segment has already been initialised")
        }
    }

    private var backUrlOverride: (() -> String?)? = null
    private var nextDestinationProvider: ((mode: TMode) -> Destination)? = null

    private var parentageProvider: (() -> Parentage)? = null
    private var additionalConfig: (TStep.() -> Unit)? = null
    private var unreachableStepDestination: (() -> Destination)? = null

    private var additionalContentProviders: MutableList<() -> Pair<String, Any>> = mutableListOf()

    fun nextStep(nextStepProvider: (mode: TMode) -> JourneyStep<*, *, *>): StepInitialiser<TStep, TState, TMode> {
        if (nextDestinationProvider != null) {
            throw JourneyInitialisationException("Step $segment already has a next destination defined")
        }
        nextDestinationProvider = { mode -> Destination(nextStepProvider(mode)) }
        return this
    }

    fun nextUrl(nextUrlProvider: (mode: TMode) -> String): StepInitialiser<TStep, TState, TMode> {
        if (nextDestinationProvider != null) {
            throw JourneyInitialisationException("Step $segment already has a next destination defined")
        }
        nextDestinationProvider = { mode -> Destination.ExternalUrl(nextUrlProvider(mode)) }
        return this
    }

    fun nextDestination(destinationProvider: (mode: TMode) -> Destination): StepInitialiser<TStep, TState, TMode> {
        if (nextDestinationProvider != null) {
            throw JourneyInitialisationException("Step $segment already has a next destination defined")
        }
        nextDestinationProvider = destinationProvider
        return this
    }

    fun noNextDestination(): StepInitialiser<TStep, TState, TMode> {
        if (nextDestinationProvider != null) {
            throw JourneyInitialisationException("Step $segment already has a next destination defined")
        }
        nextDestinationProvider = { throw PrsdbWebException("Step $segment has no next destination so cannot be posted to") }
        return this
    }

    fun parents(currentParentage: () -> Parentage): StepInitialiser<TStep, TState, TMode> {
        if (parentageProvider != null) {
            throw JourneyInitialisationException("Step $segment already has parentage defined")
        }
        parentageProvider = currentParentage
        return this
    }

    fun initialStep(): StepInitialiser<TStep, TState, TMode> = parents { NoParents() }

    fun stepSpecificInitialisation(configure: TStep.() -> Unit): StepInitialiser<TStep, TState, TMode> {
        if (additionalConfig != null) {
            throw JourneyInitialisationException("Step $segment already has additional configuration defined")
        }
        additionalConfig = configure
        return this
    }

    fun backUrl(backUrlProvider: () -> String?): StepInitialiser<TStep, TState, TMode> {
        if (backUrlOverride != null) {
            throw JourneyInitialisationException("Step $segment already has an explicit backUrl defined")
        }
        backUrlOverride = backUrlProvider
        return this
    }

    fun unreachableStepUrl(getDestination: () -> String): StepInitialiser<TStep, TState, TMode> {
        if (unreachableStepDestination != null) {
            throw JourneyInitialisationException("Step $segment already has an unreachableStepDestination defined")
        }
        unreachableStepDestination = { Destination.ExternalUrl(getDestination()) }
        return this
    }

    fun unreachableStepDestinationIfNotSet(getDestination: () -> Destination): StepInitialiser<TStep, TState, TMode> {
        if (unreachableStepDestination != null) {
            return this
        }
        unreachableStepDestination = getDestination
        return this
    }

    fun withAdditionalContentProperty(getAdditionalContent: () -> Pair<String, Any>): StepInitialiser<TStep, TState, TMode> {
        additionalContentProviders.add(getAdditionalContent)
        return this
    }

    override fun buildSteps(): List<JourneyStep<*, *, *>> = listOf(build(state))

    override fun configureSteps(configuration: StepInitialiser<*, *, *>.() -> Unit) {
        configuration()
    }

    private fun build(state: TState): JourneyStep<TMode, *, TState> {
        val parentage = parentageProvider?.invoke() ?: throw JourneyInitialisationException("Step $segment has no parentage defined")
        checkForUninitialisedParents(parentage.potentialParents)

        step.initialize(
            segment,
            state,
            backUrlOverride,
            nextDestinationProvider ?: throw JourneyInitialisationException("Step $segment has no nextDestination defined"),
            parentage,
            unreachableStepDestination
                ?: throw JourneyInitialisationException(
                    "Step $segment has no unreachableStepDestination defined, and there is no default set at the journey level either",
                ),
        ) {
            additionalContentProviders.associate { provider -> provider() }
        }

        if (step.initialisationStage == StepInitialisationStage.UNINITIALISED) {
            throw JourneyInitialisationException("Step $segment base class has not been initialised correctly")
        }

        // TODO PRSD-1546: Fix generic typing so this cast is not required
        additionalConfig?.let { configure -> (step.stepConfig as? TStep)?.configure() }
        if (step.initialisationStage != StepInitialisationStage.FULLY_INITIALISED) {
            throw JourneyInitialisationException("Custom configuration for Step $segment has not fully initialised the step")
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
                "Step $segment has uninitialised potential parents on initialisation: $parentNames\n" +
                    "This could imply a dependency loop, or that these two steps are declared in the wrong order.",
            )
        }
    }
}
