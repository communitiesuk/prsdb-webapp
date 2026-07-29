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

    open fun section(init: SectionBuilder<TState>.() -> Unit) {
        val sectionBuilder = SectionBuilder(journey, this)
        sectionBuilder.init()
        sectionBuilder.validateHeadingSet()
        sectionBuilder.applySectionHeader()
        registerTransparentBuilder(sectionBuilder)
    }

    private fun getSectionHeaderViewModel(
        headingMessageKey: String,
        useNumbering: Boolean,
    ): SectionHeaderViewModel {
        val sectionIndex = sections.indexOf(headingMessageKey) + 1
        val totalSections = sections.size
        return SectionHeaderViewModel(headingMessageKey, sectionIndex, totalSections, useNumbering)
    }

    // A section groups steps and tasks under a shared numbered heading. It owns its elements (like an embedded
    // sub-journey) and splices them transparently into the parent journey, so the parent's configuration still
    // applies to them and additional element types (e.g. embed) become available inside a section. The section
    // header is applied to every owned element via a single lazy content provider, so numbering reflects the
    // final total once all sections have been declared.
    class SectionBuilder<TState : JourneyState>(
        journey: TState,
        private val parent: JourneyBuilder<TState>,
    ) : JourneyBuilder<TState>(journey) {
        private lateinit var headingMessageKey: String
        private var useNumbering: Boolean = true

        // Sections are flat, top-level groupings on the journey's task list; nesting them would register the inner
        // heading against the wrong `sections` list (breaking numbering) and mask the inner header with the outer's.
        override fun section(init: SectionBuilder<TState>.() -> Unit): Unit =
            throw JourneyInitialisationException("Sections cannot be nested")

        fun withHeadingMessageKey(
            key: String,
            shouldUseNumbering: Boolean = true,
        ) {
            parent.sections.add(key)
            headingMessageKey = key
            useNumbering = shouldUseNumbering
        }

        internal fun applySectionHeader() {
            configure {
                withAdditionalContentProperty {
                    "sectionHeaderInfo" to parent.getSectionHeaderViewModel(headingMessageKey, useNumbering)
                }
            }
        }

        fun validateHeadingSet() {
            if (!::headingMessageKey.isInitialized || !parent.sections.contains(headingMessageKey)) {
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
