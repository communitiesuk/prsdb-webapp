package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationComplete
import uk.gov.communities.prsdb.webapp.journeys.NavigationalStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.Task

open class JourneyBuilder<TState : JourneyState>(
    // The state is referred to here as the "journey" so that in the DSL steps can be referenced as `journey.stepName`
    val journey: TState,
) {
    protected fun getStepInitialisers() = stepsUnderConstruction.toList()

    private val stepsUnderConstruction: MutableList<StepInitialiser<*, TState, *>> = mutableListOf()
    private var unreachableStepDestination: (() -> Destination)? = null

    fun build(): Map<String, StepLifecycleOrchestrator> =
        buildMap {
            stepsUnderConstruction.forEach { step ->
                val journeyStep = step.build(journey, unreachableStepDestination)
                checkForUninitialisedParents(step)
                when (journeyStep) {
                    is JourneyStep.VisitableStep<*, *, TState> -> put(journeyStep.routeSegment, StepLifecycleOrchestrator(journeyStep))
                    is JourneyStep.NotionalStep<*, *, TState> -> {}
                }
            }
        }

    fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> step(
        segment: String,
        uninitialisedStep: JourneyStep.VisitableStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        val stepInitialiser = StepInitialiser<TStep, TState, TMode>(segment, uninitialisedStep)
        stepInitialiser.init()
        stepsUnderConstruction.add(stepInitialiser)
    }

    fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> notionalStep(
        uninitialisedStep: JourneyStep.NotionalStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        val stepInitialiser = StepInitialiser<TStep, TState, TMode>(null, uninitialisedStep)
        stepInitialiser.init()
        stepsUnderConstruction.add(stepInitialiser)
    }

    fun task(
        uninitialisedTask: Task<TState>,
        init: TaskInitialiser<TState>.() -> Unit,
    ) {
        val taskInitialiser = TaskInitialiser(uninitialisedTask)
        taskInitialiser.init()
        val taskSteps = taskInitialiser.mapToStepInitialisers(journey)
        stepsUnderConstruction.addAll(taskSteps)
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

    private fun checkForUninitialisedParents(stepInitialiser: StepInitialiser<*, *, *>) {
        val uninitialisedParents =
            stepInitialiser.potentialParents.filter {
                it.initialisationStage != StepInitialisationStage.FULLY_INITIALISED
            }
        if (uninitialisedParents.any()) {
            val parentNames = uninitialisedParents.joinToString { "\n- $it" }
            throw JourneyInitialisationException(
                "Step ${stepInitialiser.segment} has uninitialised potential parents on initialisation: $parentNames\n" +
                    "This could imply a dependency loop, or that these two steps are declared in the wrong order.",
            )
        }
    }

    companion object {
        fun <TState : JourneyState> journey(
            state: TState,
            init: JourneyBuilder<TState>.() -> Unit,
        ): Map<String, StepLifecycleOrchestrator> {
            val builder = JourneyBuilder(state)
            builder.init()
            return builder.build()
        }
    }
}

class SubJourneyBuilder<TState : JourneyState>(
    journey: TState,
) : JourneyBuilder<TState>(journey) {
    private var exitInitialiser: StepInitialiser<NavigationalStepConfig, TState, NavigationComplete>? = null
    val exitStep = NavigationalStep(NavigationalStepConfig())
    lateinit var firstStep: JourneyStep<*, *, *>
        private set

    lateinit var subJourneyParentage: Parentage
        private set

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
        uninitialisedStep: JourneyStep.VisitableStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        firstStep = uninitialisedStep
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
