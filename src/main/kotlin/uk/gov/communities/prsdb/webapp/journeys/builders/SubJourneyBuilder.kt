package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationComplete
import uk.gov.communities.prsdb.webapp.journeys.NavigationalStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Parentage

class SubJourneyBuilder<TState : JourneyState>(
    journey: TState,
) : JourneyBuilder<TState>(journey) {
    private var exitInitialiser: StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>? = null
    val exitStep = NavigationalStep(NavigationalStepConfig())
    lateinit var firstStep: JourneyStep<*, *, *>
        private set

    private lateinit var subJourneyParentage: Parentage

    fun exitStep(init: StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>.() -> Unit) {
        if (exitInitialiser != null) {
            throw JourneyInitialisationException("Sub-journey already has an exit step defined")
        }
        val stepInitialiser = StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>(null, exitStep)
        stepInitialiser.init()
        exitInitialiser = stepInitialiser
    }

    fun subJourneyParent(parentage: Parentage) {
        if (::subJourneyParentage.isInitialized) {
            throw JourneyInitialisationException("Sub-journey exit step parentage has already been defined")
        }
        subJourneyParentage = parentage
    }

    fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> startingStep(
        segment: String,
        uninitialisedStep: JourneyStep.RoutedStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        firstStep = uninitialisedStep
        if (!::subJourneyParentage.isInitialized) {
            throw JourneyInitialisationException("Sub-journey parentage must be defined before the starting step")
        }
        return step<TMode, TStep>(segment, uninitialisedStep) {
            parents { subJourneyParentage }
            init()
        }
    }

    fun getSteps(
        exitInit: StepInitialiser<NavigationalStepConfig, *, NavigationComplete>.() -> Unit,
    ): List<StepInitialiser<*, TState, *>> {
        val exitStepInitialiser = exitInitialiser ?: throw JourneyInitialisationException("Sub-journey must have an exit step defined")
        if (!::firstStep.isInitialized) throw JourneyInitialisationException("Sub-journey must have a first step defined")

        exitStepInitialiser.exitInit()
        return getStepInitialisers() + exitStepInitialiser
    }

    companion object {
        fun <TState : JourneyState> subJourney(
            state: TState,
            exitInit: StepInitialiser<NavigationalStepConfig, *, NavigationComplete>.() -> Unit,
            init: SubJourneyBuilder<TState>.() -> Unit,
        ): List<StepInitialiser<*, TState, *>> {
            val builder = SubJourneyBuilder(state)
            builder.init()
            return builder.getSteps(exitInit)
        }
    }
}
