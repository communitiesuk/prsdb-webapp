package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.VisitableJourneyElement
import uk.gov.communities.prsdb.webapp.journeys.example.Destination

class JourneyBuilder<TState : JourneyState>(
    // The state is referred to here as the "journey" so that in the DSL steps can be referenced as `journey.stepName`
    val journey: TState,
) {
    private val stepsUnderConstruction: MutableList<StepInitialiser<*, TState, *>> = mutableListOf()
    private var unreachableStepDestination: (() -> Destination)? = null

    fun build(): Map<String, VisitableJourneyElement> =
        stepsUnderConstruction
            .mapNotNull { sb ->
                sb.build(journey, unreachableStepDestination).let {
                    checkForUninitialisedParents(sb)
                    when (sb.notionalStep) {
                        true -> null
                        false -> it
                    }
                }
            }.associate { it.routeSegment to StepLifecycleOrchestrator(it) }

    fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> step(
        segment: String,
        uninitialisedStep: JourneyStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        val stepInitialiser = StepInitialiser<TStep, TState, TMode>(segment, uninitialisedStep)
        stepInitialiser.init()
        stepsUnderConstruction.add(stepInitialiser)
    }

    fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> notionalStep(
        segment: String,
        uninitialisedStep: JourneyStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    ) {
        val stepInitialiser = StepInitialiser<TStep, TState, TMode>(segment, uninitialisedStep, true)
        stepInitialiser.init()
        stepsUnderConstruction.add(stepInitialiser)
    }

    fun <TMode : Enum<TMode>> task(
        // TODO PRSD-1546 remove the exitRoute parameter when tasks have been migrated to use a Destination
        exitRoute: String,
        uninitialisedTask: Task<TMode, TState>,
        init: TaskInitialiser<TMode, TState>.() -> Unit,
    ) {
        val taskInitialiser = TaskInitialiser(uninitialisedTask)
        taskInitialiser.init()
        val taskSteps = taskInitialiser.mapToStepInitialisers(exitRoute, journey)
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
        ): Map<String, VisitableJourneyElement> {
            val builder = JourneyBuilder(state)
            builder.init()
            return builder.build()
        }
    }
}
