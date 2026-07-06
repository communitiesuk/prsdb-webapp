package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.InstanceableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.urlPath
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel

interface JourneyBuilderDsl<TState : JourneyState> {
    fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> step(
        uninitialisedStep: JourneyStep<TMode, *, TState>,
        init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
    )

    fun task(
        uninitialisedTask: Task<TState>,
        init: TaskInitialiser<TState>.() -> Unit,
    )

    // Adds a task that carries its own state, isolated under `routeSegment`, so the same task can be added more
    // than once. Unlike `task`, the task's state type is independent of the journey's, so a shared task (e.g. the
    // address task) can be reused for a second instance without the journey state gaining per-instance fields.
    fun <TTaskState : JourneyState, T> instancedTask(
        uninitialisedTask: T,
        routeSegment: String,
        init: TaskInitialiser<TTaskState>.() -> Unit,
    ) where T : Task<TTaskState>, T : InstanceableTask<TTaskState>
}

open class JourneyBuilder<TState : JourneyState>(
    // The state is referred to here as the "journey" so that in the DSL steps can be referenced as `journey.stepName`
    journey: TState,
) : AbstractJourneyBuilder<TState>(journey) {
    private val sections: MutableList<String> = mutableListOf()

    fun buildRoutingMap(): Map<String, StepLifecycleOrchestrator> =
        buildMap {
            build().forEach { journeyStep ->
                when (journeyStep) {
                    is JourneyStep.RequestableStep<*, *, *> -> put(journeyStep.urlPath, journeyStep.lifecycleOrchestrator)
                    is JourneyStep.InternalStep<*, *> -> return@forEach
                }
            }
        }

    fun section(init: SectionBuilder<TState>.() -> Unit) {
        val sectionBuilder = SectionBuilder<TState>(this)
        sectionBuilder.init()
        sectionBuilder.validateHeadingSet()
    }

    class SectionBuilder<TState : JourneyState>(
        private val journeyBuilder: JourneyBuilder<TState>,
    ) : JourneyBuilderDsl<TState> {
        private lateinit var headingMessageKey: String
        private var useNumbering: Boolean = true

        fun withHeadingMessageKey(
            key: String,
            shouldUseNumbering: Boolean = true,
        ) {
            journeyBuilder.sections.add(key)
            headingMessageKey = key
            useNumbering = shouldUseNumbering
        }

        override fun <TMode : Enum<TMode>, TStep : AbstractStepConfig<TMode, *, TState>> step(
            uninitialisedStep: JourneyStep<TMode, *, TState>,
            init: StepInitialiser<TStep, TState, TMode>.() -> Unit,
        ) = journeyBuilder.step<TMode, TStep>(uninitialisedStep) {
            init()
            withAdditionalContentProperty {
                "sectionHeaderInfo" to journeyBuilder.getSectionHeaderViewModel(headingMessageKey, useNumbering)
            }
        }

        override fun task(
            uninitialisedTask: Task<TState>,
            init: TaskInitialiser<TState>.() -> Unit,
        ) = journeyBuilder.task(uninitialisedTask) {
            init()
            withAdditionalContentProperty {
                "sectionHeaderInfo" to journeyBuilder.getSectionHeaderViewModel(headingMessageKey, useNumbering)
            }
        }

        override fun <TTaskState : JourneyState, T> instancedTask(
            uninitialisedTask: T,
            routeSegment: String,
            init: TaskInitialiser<TTaskState>.() -> Unit,
        ) where T : Task<TTaskState>, T : InstanceableTask<TTaskState> =
            journeyBuilder.instancedTask(uninitialisedTask, routeSegment) {
                init()
                withAdditionalContentProperty {
                    "sectionHeaderInfo" to journeyBuilder.getSectionHeaderViewModel(headingMessageKey, useNumbering)
                }
            }

        private fun JourneyBuilder<*>.getSectionHeaderViewModel(
            headingMessageKey: String,
            useNumbering: Boolean,
        ): SectionHeaderViewModel {
            val sectionIndex = sections.indexOf(headingMessageKey) + 1
            val totalSections = sections.size
            return SectionHeaderViewModel(headingMessageKey, sectionIndex, totalSections, useNumbering)
        }

        fun validateHeadingSet() {
            if (!::headingMessageKey.isInitialized || !journeyBuilder.sections.contains(headingMessageKey)) {
                throw JourneyInitialisationException("Section heading message key must be set using withHeadingMessageKey")
            }
        }
    }

    companion object {
        fun <TState : JourneyState> journey(
            state: TState,
            init: JourneyBuilder<TState>.() -> Unit,
        ): Map<String, StepLifecycleOrchestrator> {
            val builder = JourneyBuilder(state)
            builder.init()
            return builder.buildRoutingMap()
        }
    }
}
