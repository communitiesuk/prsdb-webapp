package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInnerStep
import uk.gov.communities.prsdb.webapp.journeys.DynamicJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator

class JourneyBuilder<TJourney : DynamicJourneyState>(
    val journey: TJourney,
) {
    private val stepsUnderConstruction: MutableList<StepBuilder<*, TJourney, *>> = mutableListOf()
    private var unreachableStepRedirect: (() -> String)? = null

    fun build(): Map<String, StepLifecycleOrchestrator> =
        stepsUnderConstruction.associate { sb ->
            sb.build(journey, unreachableStepRedirect).let {
                checkForUninitialisedParents(sb)
                it.routeSegment to StepLifecycleOrchestrator(it)
            }
        }

    fun <TMode : Enum<TMode>, TStep : AbstractInnerStep<TMode, *, TJourney>> step(
        segment: String,
        uninitialisedStep: JourneyStep<TMode, *, TJourney>,
        init: StepBuilder<TStep, TJourney, TMode>.() -> Unit,
    ) {
        val stepBuilder = StepBuilder<TStep, TJourney, TMode>(segment, uninitialisedStep)
        stepBuilder.init()
        stepsUnderConstruction.add(stepBuilder)
    }

    fun unreachableStepRedirect(getRedirect: () -> String) {
        if (unreachableStepRedirect != null) {
            throw JourneyInitialisationException("unreachableStepRedirect has already been set")
        }
        unreachableStepRedirect = getRedirect
    }

    private fun checkForUninitialisedParents(stepBuilder: StepBuilder<*, *, *>) {
        val uninitialisedParents =
            stepBuilder.potentialParents.filter {
                it.initialisationStage != StepInitialisationStage.FULLY_INITIALISED
            }
        if (uninitialisedParents.any()) {
            val parentNames = uninitialisedParents.joinToString { "\n- $it" }
            throw JourneyInitialisationException(
                "Step ${stepBuilder.segment} has uninitialised potential parents on initialisation: $parentNames\n" +
                    "This could imply a dependency loop, or that these two steps are declared in the wrong order.",
            )
        }
    }

    companion object {
        fun <TState : DynamicJourneyState> journey(
            state: TState,
            init: JourneyBuilder<TState>.() -> Unit,
        ): Map<String, StepLifecycleOrchestrator> {
            val builder = JourneyBuilder(state)
            builder.init()
            return builder.build()
        }
    }
}
