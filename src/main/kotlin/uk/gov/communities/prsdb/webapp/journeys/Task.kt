package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.constants.ReservedTagValues
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.builders.ConfigurableElement
import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiser
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder

// The single task base. A task owns its own JourneyState and namespaces its stored data behind a route prefix,
// so the same task can be added to a journey more than once, each instance isolated by its route. A null route
// keeps bare keys.
//
// Subclasses supply the task's steps, its task-specific state, and makeSubJourney; this base owns the route
// binding and key-registry wiring (the actual scoping is done by the inherited delegateProvider).
//
// If a task needs typed access to the enclosing journey/sibling state, it declares a TDependencies contract; the
// mount site binds the live state via withDependencies { }. Tasks with no such need use TaskWithoutDependencies
// (TDependencies = Nothing, requiresDependencies = false).
abstract class Task<TState : JourneyState, TDependencies : Any>(
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService) {
    lateinit var subJourneyBuilder: SubJourneyBuilder<*>
        private set
    private lateinit var exitInit: StepInitialiser<SubjourneyExitStepConfig, *, SubjourneyComplete>.() -> Unit
    private var exitStepOverride: SubjourneyExitStep? = null

    abstract val taskState: TState

    // Whether this task must have its dependencies bound at the mount site. True by default;
    // TaskWithoutDependencies overrides it to false so a bare task(...) { } call needs no
    // withDependencies { }.
    open val requiresDependencies: Boolean = true

    // Nullable backing field rather than lateinit so that TaskWithoutDependencies (TDependencies = Nothing) is legal
    private var boundDependencies: TDependencies? = null

    val areDependenciesBound: Boolean get() = boundDependencies != null

    // The typed, live reference to the enclosing dependencies, bound at build time by the mount site. Reads
    // reflect later mutations to the enclosing state because it holds the state instance itself.
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
