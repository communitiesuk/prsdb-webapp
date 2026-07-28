package uk.gov.communities.prsdb.webapp.journeys.builders

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.AbstractStepConfig
import uk.gov.communities.prsdb.webapp.journeys.DelegateKeyRegistry
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTaskWithDependencies
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
        routeSegment: String? = null,
        init: TaskInitialiser<TState, Nothing>.() -> Unit,
    )

    // Adds a self-stated task: one that owns its own steps and acts as its own state. It is bound to the journey's
    // state (for data storage) and an optional `routeSegment`, letting the same task be added more than once
    // (each instance isolated by its route) without the journey state gaining per-instance fields. When
    // `routeSegment` is null the task's steps keep bare URLs and data keys. A task declaring a TDependencies contract
    // binds the enclosing state via withDependencies { }; dependency-free tasks (TDependencies = Nothing) do not.
    fun <TTaskState : JourneyState, TDependencies : Any> duplicableTask(
        uninitialisedTask: DuplicableTaskWithDependencies<TTaskState, TDependencies>,
        routeSegment: String? = null,
        init: TaskInitialiser<TTaskState, TDependencies>.() -> Unit,
    )
}

open class JourneyBuilder<TState : JourneyState>(
    // The state is referred to here as the "journey" so that in the DSL steps can be referenced as `journey.stepName`
    override val journey: TState,
) : AbstractJourneyBuilder<TState, TState>(journey) {
    private val sections: MutableList<String> = mutableListOf()

    fun buildRoutingMap(): Map<String, StepLifecycleOrchestrator> =
        buildMap {
            // One registry for the whole build. Bind the journey state first (its keys are bare - the root state has
            // no route), then thread it through build() so every task registers its route-scoped keys into it and
            // any cross-element key collision throws here, at build time.
            val registry = DelegateKeyRegistry()
            journey.bindKeyRegistry(registry)
            build(registry).forEach { journeyStep ->
                when (journeyStep) {
                    is JourneyStep.RequestableStep<*, *, *> -> {
                        put(
                            journeyStep.urlPath,
                            journeyStep.lifecycleOrchestrator,
                        )
                    }

                    is JourneyStep.InternalStep<*, *> -> {
                        return@forEach
                    }
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
            routeSegment: String?,
            init: TaskInitialiser<TState, Nothing>.() -> Unit,
        ) = journeyBuilder.task(uninitialisedTask, routeSegment) {
            init()
            withAdditionalContentProperty {
                "sectionHeaderInfo" to journeyBuilder.getSectionHeaderViewModel(headingMessageKey, useNumbering)
            }
        }

        override fun <TTaskState : JourneyState, TDependencies : Any> duplicableTask(
            uninitialisedTask: DuplicableTaskWithDependencies<TTaskState, TDependencies>,
            routeSegment: String?,
            init: TaskInitialiser<TTaskState, TDependencies>.() -> Unit,
        ) = journeyBuilder.duplicableTask(uninitialisedTask, routeSegment) {
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
