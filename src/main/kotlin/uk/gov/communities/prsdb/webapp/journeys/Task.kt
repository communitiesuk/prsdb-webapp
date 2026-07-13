package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.constants.ReservedTagValues
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.builders.ConfigurableElement
import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiser
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder

abstract class Task<in TState : JourneyState> : RegistersDelegateKeys {
    lateinit var subJourneyBuilder: SubJourneyBuilder<*>
        private set
    private lateinit var exitInit: StepInitialiser<SubjourneyExitStepConfig, *, SubjourneyComplete>.() -> Unit
    private var exitStepOverride: SubjourneyExitStep? = null

    fun getTaskSubJourneyBuilder(
        state: TState,
        exitInit: StepInitialiser<SubjourneyExitStepConfig, *, SubjourneyComplete>.() -> Unit,
    ): SubJourneyBuilder<*> {
        this.exitInit = exitInit
        return makeSubJourney(state)
    }

    protected fun <TDslState : TState> subJourney(
        state: TDslState,
        init: SubJourneyBuilder<TDslState>.() -> Unit,
    ): SubJourneyBuilder<TDslState> {
        if (::subJourneyBuilder.isInitialized) {
            throw JourneyInitialisationException("Task sub-journey has already been initialised")
        }
        val localSubJourneyBuilder = SubJourneyBuilder(state, exitStepOverride)
        subJourneyBuilder = localSubJourneyBuilder
        localSubJourneyBuilder.exitStep {
            savable()
            exitInit()
        }
        localSubJourneyBuilder.init()
        return localSubJourneyBuilder
    }

    fun setCustomExitStep(step: SubjourneyExitStep) {
        if (::subJourneyBuilder.isInitialized) {
            throw JourneyInitialisationException("Cannot set custom exit step after sub-journey has been initialised")
        }
        this.exitStepOverride = step
    }

    abstract fun makeSubJourney(state: TState): SubJourneyBuilder<*>

    // A duplicable task (one that owns its own steps and acts as its own state) sources its
    // JourneyState behaviour from its own journeyStateService; the only value it needs at
    // build time is its route prefix, used to namespace its stored data keys. The TaskInitialiser
    // calls this from build(). The default no-op keeps journey-stated tasks unaffected.
    // End state: once every task is duplicable, this stops being open/no-op and the route becomes
    // a plain field the TaskInitialiser always populates.
    open fun bindRoute(routePrefix: String?) {}

    // A task that owns delegate keys (a duplicable task) attaches its own provider to the journey-build-wide
    // DelegateKeyRegistry here, so its route-scoped keys are checked for collisions against the journey state and
    // every other task. The TaskInitialiser calls this from build(), AFTER bindRoute so keys resolve to their final
    // route-scoped form. The default no-op keeps journey-stated tasks (which own no keys) unaffected.
    override fun bindKeyRegistry(registry: DelegateKeyRegistry) {}

    fun taskStatus(): TaskStatus = subJourneyBuilder.taskStatusOverride?.invoke() ?: defaultTaskStatus()

    private fun defaultTaskStatus(): TaskStatus =
        when {
            exitStep.isStepReachable -> TaskStatus.COMPLETED
            firstStep.outcome != null -> TaskStatus.IN_PROGRESS
            firstStep.isStepReachable -> TaskStatus.NOT_STARTED
            else -> TaskStatus.CANNOT_START
        }

    val exitStep: SubjourneyExitStep get() = subJourneyBuilder.exitStep
    val firstStep: JourneyStep<*, *, *> get() = subJourneyBuilder.firstStep

    protected fun ConfigurableElement<*>.savable() {
        taggedWith(SAVABLE)
    }

    companion object {
        fun SubJourneyBuilder<*>.configureSavable(init: ConfigurableElement<*>.() -> Unit) = configureTagged(SAVABLE, init)

        private const val SAVABLE = ReservedTagValues.SAVABLE
    }
}
