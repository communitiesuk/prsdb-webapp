package uk.gov.communities.prsdb.webapp.journeys

// Base for "self-stated" tasks: tasks that own their own JourneyState rather than binding to an external one, and
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
abstract class SelfStatedRoutableTask<TState : JourneyState>(
    // Self-made journey-state delegate over the task's OWN journeyStateService. Because JourneyStateService
    // resolves the active session from the request, this reads/writes the same journey data as the journey root
    // state - so no external delegate needs binding, only the route.
    journeyStateService: JourneyStateService,
) : Task<TState>(),
    JourneyState by object : AbstractJourneyState(journeyStateService) {} {
    // Delegate provider over the task's own journeyStateService, used by subclasses to create route-scoped
    // nullable delegates.
    protected val delegateProvider = JourneyStateDelegateProvider(journeyStateService)

    // Route-only late binding - the sole value the TaskInitialiser supplies at build time.
    override fun bindRoute(routePrefix: String?) = delegateProvider.bindRoutePrefix(routePrefix)

    // Attach this task's own provider to the shared registry, flushing its route-scoped keys. Called by the
    // TaskInitialiser AFTER bindRoute, so keys resolve to their final route-scoped form before registration.
    override fun bindKeyRegistry(registry: DelegateKeyRegistry) = delegateProvider.bindKeyRegistry(registry)

    abstract val taskState: TState
}
