package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.constants.ReservedTagValues
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.builders.ConfigurableElement
import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiser
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder

/**
 * Base class for all tasks in the journey framework
 *
 * All tasks inherit from this class either directly or via TaskWithoutDependencies. Add them to a journey
 * DSL with the `task` function. Implementors must specify the `taskState`, usually by implementing their
 * own state interface and returning `this`. They must also implement the `makeSubJourney(state)` function
 * by calling `subJourney(state) { <DSL> }` and specifying the Task structure in DSL.
 *
 * @property dependencies External dependencies, normally the enclosing journey or task
 */
abstract class Task<TState : JourneyState, TDependencies : Any>(
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService) {
    lateinit var subJourneyBuilder: SubJourneyBuilder<*>
        private set
    private lateinit var exitInit: StepInitialiser<SubjourneyExitStepConfig, *, SubjourneyComplete>.() -> Unit
    private var exitStepOverride: SubjourneyExitStep? = null

    abstract val taskState: TState

    open val requiresDependencies: Boolean = true

    private var boundDependencies: TDependencies? = null

    val areDependenciesBound: Boolean get() = boundDependencies != null

    val dependencies: TDependencies
        get() = boundDependencies ?: throw UninitializedPropertyAccessException("dependencies have not been bound")

    fun bindDependencies(value: TDependencies) {
        if (areDependenciesBound) {
            throw JourneyInitialisationException("dependencies have already been bound")
        }
        boundDependencies = value
    }

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

    protected abstract fun makeSubJourney(state: TState): SubJourneyBuilder<*>

    // Route-only late binding - the sole value the TaskInitialiser supplies at build time.
    // Key-registry binding is inherited from AbstractJourneyState via `DelegateKeysOwner by delegateProvider`.
    fun bindRoute(routePrefix: String?) = delegateProvider.bindRoutePrefix(routePrefix)

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
