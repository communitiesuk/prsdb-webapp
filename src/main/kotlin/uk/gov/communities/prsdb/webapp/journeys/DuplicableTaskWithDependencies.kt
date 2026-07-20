package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException

// Base for "Duplicable" tasks: tasks that own their own JourneyState rather than binding to an external one, and
// namespace their stored data behind a route prefix so the same task can be added to a journey more than once, each
// instance isolated by its route. All journey-level JourneyState behaviour is sourced from the task's OWN
// journeyStateService (via a self-made AbstractJourneyState delegate); only the route prefix is bound, at build
// time, via bindRoute. A null route keeps bare keys (preserving pre-existing storage).
//
// Subclasses supply the task's steps, its task-specific state and makeSubJourney; this base owns the route-scoping
// machinery. Subclasses create route-scoped nullable delegates via delegateProvider.nullableDelegate { scopedKey(...) }.
//
// This base's provider participates in the journey-build-wide DelegateKeyRegistry (see bindKeyRegistry), so its
// route-scoped keys are checked for collisions against the journey state's keys and every other task's keys at
// build time.
//
// In addition, this base carries an optional typed reference to the state it is mounted in: `dependencies`. A task
// that needs typed access to values owned by the enclosing journey/sibling state (values it cannot compute itself)
// declares a TDependencies contract, surfaces `dependencies` on its own state interface, and reads through it. The
// mount site binds the live enclosing state via `withDependencies { journey }`. A task that needs no such access
// uses DuplicableTask (TDependencies = Nothing, requiresDependencies = false).
abstract class DuplicableTaskWithDependencies<TState : JourneyState, TDependencies : Any>(
    journeyStateService: JourneyStateService,
) : Task<TState>(),
    JourneyState by object : AbstractJourneyState(journeyStateService) {} {
    protected val delegateProvider = JourneyStateDelegateProvider(journeyStateService)

    // Route-only late binding - the sole value the TaskInitialiser supplies at build time.
    override fun bindRoute(routePrefix: String?) = delegateProvider.bindRoutePrefix(routePrefix)

    // Attach this task's own provider to the shared registry, flushing its route-scoped keys. Called by the
    // TaskInitialiser AFTER bindRoute, so keys resolve to their final route-scoped form before registration.
    override fun bindKeyRegistry(registry: DelegateKeyRegistry) = delegateProvider.bindKeyRegistry(registry)

    abstract val taskState: TState

    // Whether this task must have its dependencies bound at the mount site. True here; DuplicableTask (which has no
    // dependencies) overrides it to false so a bare duplicableTask(...) { } call needs no withDependencies { }.
    open val requiresDependencies: Boolean = true

    // Nullable backing field rather than lateinit so that DuplicableTask (TDependencies = Nothing) is legal: for a
    // plain task this is always null and `dependencies` is never read.
    private var boundDependencies: TDependencies? = null

    val areDependenciesBound: Boolean get() = boundDependencies != null

    // The typed, live reference to the enclosing dependencies, bound at build time by the mount site. Reads reflect
    // later mutations to the enclosing state because it holds the state instance itself.
    val dependencies: TDependencies
        get() = boundDependencies ?: throw UninitializedPropertyAccessException("dependencies have not been bound")

    fun bindDependencies(value: TDependencies) {
        if (areDependenciesBound) {
            throw JourneyInitialisationException("dependencies have already been bound")
        }
        boundDependencies = value
    }
}
