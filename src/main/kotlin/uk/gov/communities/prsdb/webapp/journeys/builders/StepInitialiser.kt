package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage

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
    private var redirectToUrl: ((mode: TMode) -> String)? = null
    private var parentage: (() -> Parentage)? = null
    private var additionalConfig: (TStep.() -> Unit)? = null
    private var unreachableStepRedirect: (() -> String)? = null

    fun redirectToStep(nextStepProvider: (mode: TMode) -> JourneyStep<*, *, TState>): StepInitialiser<TStep, TState, TMode> {
        if (redirectToUrl != null) {
            throw JourneyInitialisationException("Step $segment already has a redirectTo defined")
        }
        redirectToUrl = { mode -> nextStepProvider(mode).routeSegment }
        return this
    }

    fun redirectToUrl(nextUrlProvider: (mode: TMode) -> String): StepInitialiser<TStep, TState, TMode> {
        if (redirectToUrl != null) {
            throw JourneyInitialisationException("Step $segment already has a redirectTo defined")
        }
        redirectToUrl = nextUrlProvider
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

    fun unreachableStepRedirect(getRedirect: () -> String): StepInitialiser<TStep, TState, TMode> {
        if (unreachableStepRedirect != null) {
            throw JourneyInitialisationException("Step $segment already has an unreachableStepRedirect defined")
        }
        unreachableStepRedirect = getRedirect
        return this
    }

    fun build(
        state: TState,
        defaultUnreachableStepRedirect: (() -> String)?,
    ): JourneyStep<TMode, *, TState> {
        step.initialize(
            segment,
            state,
            backUrlOverride,
            redirectToUrl ?: throw JourneyInitialisationException("Step $segment has no redirectTo defined"),
            parentage?.invoke() ?: NoParents(),
            unreachableStepRedirect
                ?: defaultUnreachableStepRedirect
                ?: throw JourneyInitialisationException(
                    "Step $segment has no unreachableStepRedirect defined, and there is no default set at the journey level either",
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
