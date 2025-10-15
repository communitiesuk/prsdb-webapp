package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.journeys.AbstractInnerStep
import uk.gov.communities.prsdb.webapp.journeys.DynamicJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage

class StepBuilder<TStep : AbstractInnerStep<TMode, *, TState>, TState : DynamicJourneyState, TMode : Enum<TMode>>(
    val segment: String,
    private val step: JourneyStep<TMode, *, TState>,
) {
    init {
        if (step.initialisationStage != StepInitialisationStage.UNINITIALISED) {
            throw Exception("Step $segment has already been initialised")
        }
    }

    private var backUrlOverride: (() -> String?)? = null
    private var redirectToUrl: ((mode: TMode) -> String)? = null
    private var parentage: (() -> Parentage)? = null
    private var additionalConfig: (TStep.() -> Unit)? = null
    private var stepUnreachableStepRedirect: (() -> String)? = null

    fun redirectToStep(nextStepProvider: (mode: TMode) -> JourneyStep<*, *, TState>): StepBuilder<TStep, TState, TMode> {
        if (redirectToUrl != null) {
            throw Exception("Step $segment already has a redirectTo defined")
        }
        redirectToUrl = { mode -> nextStepProvider(mode).routeSegment }
        return this
    }

    fun redirectToUrl(nextUrlProvider: (mode: TMode) -> String): StepBuilder<TStep, TState, TMode> {
        if (redirectToUrl != null) {
            throw Exception("Step $segment already has a redirectTo defined")
        }
        redirectToUrl = nextUrlProvider
        return this
    }

    fun parents(currentParentage: () -> Parentage): StepBuilder<TStep, TState, TMode> {
        if (parentage != null) {
            throw Exception("Step $segment already has parentage defined")
        }
        parentage = currentParentage
        return this
    }

    fun stepSpecificInitialisation(configure: TStep.() -> Unit): StepBuilder<TStep, TState, TMode> {
        if (additionalConfig != null) {
            throw Exception("Step $segment already has additional configuration defined")
        }
        additionalConfig = configure
        return this
    }

    fun backUrl(backUrlProvider: () -> String?): StepBuilder<TStep, TState, TMode> {
        if (backUrlOverride != null) {
            throw Exception("Step $segment already has an explicit backUrl defined")
        }
        backUrlOverride = backUrlProvider
        return this
    }

    fun unreachableStepRedirect(getRedirect: () -> String): StepBuilder<TStep, TState, TMode> {
        if (stepUnreachableStepRedirect != null) {
            throw Exception("Step $segment already has an unreachableStepRedirect defined")
        }
        stepUnreachableStepRedirect = getRedirect
        return this
    }

    fun build(
        state: TState,
        defaultUnreachableStepRedirect: (() -> String)?,
    ): JourneyStep<TMode, *, TState> {
        val castedRedirectTo = redirectToUrl ?: throw Exception("Step $segment has no redirectTo defined")
        val castedParentage = parentage ?: { NoParents() }
        val unreachableStepRedirect =
            stepUnreachableStepRedirect
                ?: defaultUnreachableStepRedirect
                ?: throw Exception(
                    "Step $segment has no unreachableStepRedirect defined, and there is no default set at the journey level either",
                )

        step.initialize(segment, state, backUrlOverride, castedRedirectTo, castedParentage, unreachableStepRedirect)
        if (step.initialisationStage == StepInitialisationStage.UNINITIALISED) {
            throw Exception("Step $segment base class has not been initialised correctly")
        }
        // TODO PRSD-1546: Fix generic typing so this cast is not required
        additionalConfig?.let { configure -> (step.innerStep as? TStep)?.configure() }
        if (step.initialisationStage != StepInitialisationStage.FULLY_INITIALISED) {
            throw Exception("Custom configuration for Step $segment has not fully initialised the step")
        }
        return step
    }

    val potentialParents: List<JourneyStep<*, *, *>>
        get() {
            if (step.initialisationStage == StepInitialisationStage.UNINITIALISED) {
                throw Exception("Step $segment has not been initialised yet")
            }

            return step.parentage.potentialParents
        }
}
