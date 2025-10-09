package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.journeys.AbstractStep
import uk.gov.communities.prsdb.webapp.journeys.DynamicJourneyState
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage

class StepBuilder<TStep : AbstractStep<TMode, *, TState, TStep>, TState : DynamicJourneyState, TMode : Enum<TMode>>(
    val segment: String,
    private val step: TStep,
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

    fun redirectToStep(nextStepProvider: (mode: TMode) -> AbstractStep<*, *, TState, *>): StepBuilder<TStep, TState, TMode> {
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

    fun build(state: TState): TStep {
        val castedRedirectTo = redirectToUrl ?: throw Exception("Step $segment has no redirectTo defined")
        val castedParentage = parentage ?: { NoParents() }
        step.initialize(segment, state, backUrlOverride, castedRedirectTo, castedParentage)
        if (step.initialisationStage == StepInitialisationStage.UNINITIALISED) {
            throw Exception("Step $segment base class has not been initialised correctly")
        }
        additionalConfig?.let { configure -> step.configure() }
        if (step.initialisationStage != StepInitialisationStage.FULLY_INITIALISED) {
            throw Exception("Custom configuration for Step $segment has not fully initialised the step")
        }
        return step
    }

    val structuralParents: List<AbstractStep<*, *, *, *>>
        get() {
            if (step.initialisationStage == StepInitialisationStage.UNINITIALISED) {
                throw Exception("Step $segment has not been initialised yet")
            }

            return step.parentage.potentialParents
        }
}
