package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.journeys.AbstractStep
import uk.gov.communities.prsdb.webapp.journeys.DynamicJourneyState
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator

class JourneyBuilder<TState : DynamicJourneyState>(
    private val state: TState,
) {
    val stepsUnderConstruction: MutableList<StepBuilder<*, TState, *>> = mutableListOf()

    fun build() =
        stepsUnderConstruction.associate { sb ->
            sb.build(state).let {
                checkForUninitialisedParents(sb)
                it.routeSegment to StepLifecycleOrchestrator(it)
            }
        }

    fun <TMode : Enum<TMode>, TStep : AbstractStep<TMode, *, TState>> step(
        segment: String,
        uninitialisedStep: TStep,
        init: StepBuilder<TStep, TState, TMode>.() -> Unit,
    ) {
        val stepBuilder = StepBuilder(segment, uninitialisedStep)
        stepBuilder.init()
        stepsUnderConstruction.add(stepBuilder)
    }

    private fun checkForUninitialisedParents(stepBuilder: StepBuilder<*, *, *>) {
        val uninitialisedParents = stepBuilder.potentialParents.filter { it.initialisationStage == StepInitialisationStage.UNINITIALISED }
        if (uninitialisedParents.any()) {
            val parentNames = uninitialisedParents.joinToString { "\n- $it" }
            throw Exception(
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
