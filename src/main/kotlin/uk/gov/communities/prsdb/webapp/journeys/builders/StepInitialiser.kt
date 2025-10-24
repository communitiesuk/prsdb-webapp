package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage
import uk.gov.communities.prsdb.webapp.journeys.example.Destination

class StepInitialiser<TStep : AbstractStepConfig<TMode, *, TState>, TState : JourneyState, TMode : Enum<TMode>>(
    val segment: String,
    private val step: JourneyStep<TMode, *, TState>,
) {
    init {
        if (step.initialisationStage != StepInitialisationStage.UNINITIALISED) {
            throw JourneyInitialisationException("Step $segment has already been initialised")
        }
    }

    private var backUrlOverride: (() -> String?)? = null
    private var nextDestinationProvider: ((mode: TMode) -> Destination)? = null
    private var parentage: (() -> Parentage)? = null
    private var additionalConfig: (TStep.() -> Unit)? = null
    private var unreachableStepDestination: (() -> Destination)? = null

    fun nextStep(nextStepProvider: (mode: TMode) -> JourneyStep<*, *, TState>): StepInitialiser<TStep, TState, TMode> {
        if (nextDestinationProvider != null) {
            throw JourneyInitialisationException("Step $segment already has a next destination defined")
        }
        nextDestinationProvider = { mode -> Destination.Step(nextStepProvider(mode)) }
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

    fun parents(currentParentage: () -> Parentage): StepInitialiser<TStep, TState, TMode> {
        if (parentage != null) {
            throw JourneyInitialisationException("Step $segment already has parentage defined")
        }
        parentage = currentParentage
        return this
    }

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
            throw JourneyInitialisationException("Step $segment already has an unreachableStepRedirect defined")
        }
        unreachableStepDestination = { Destination.ExternalUrl(getDestination()) }
        return this
    }

    fun build(
        state: TState,
        defaultUnreachableStepDestination: (() -> Destination)?,
    ): JourneyStep<TMode, *, TState> {
        step.initialize(
            segment,
            state,
            backUrlOverride,
            nextDestinationProvider ?: throw JourneyInitialisationException("Step $segment has no redirectTo defined"),
            parentage?.invoke() ?: NoParents(),
            unreachableStepDestination
                ?: defaultUnreachableStepDestination
                ?: throw JourneyInitialisationException(
                    "Step $segment has no unreachableStepDestination defined, and there is no default set at the journey level either",
                ),
        )

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

    val potentialParents: List<JourneyStep<*, *, *>>
        get() {
            if (step.initialisationStage != StepInitialisationStage.FULLY_INITIALISED) {
                throw JourneyInitialisationException("Step $segment has not been initialised yet")
            }

            return step.parentage.potentialParents
        }
}
