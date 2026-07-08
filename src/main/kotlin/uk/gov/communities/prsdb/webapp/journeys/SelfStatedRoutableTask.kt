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
// KNOWN LIMITATION: this base creates its OWN JourneyStateDelegateProvider, so route-scoped keys are not compared
// against the journey state's keys (nor other tasks' keys) for collisions. See the KNOWN LIMITATION comment on
// JourneyStateDelegateProvider.registerKey - to be addressed in a later commit.
abstract class SelfStatedRoutableTask<TState : JourneyState>(
    journeyStateService: JourneyStateService,
    // Self-made journey-state delegate over the task's OWN journeyStateService. Because JourneyStateService
    // resolves the active session from the request, this reads/writes the same journey data as the journey root
    // state - so no external delegate needs binding, only the route.
    private val stateDelegate: AbstractJourneyState = object : AbstractJourneyState(journeyStateService) {},
) : Task<TState>(),
    JourneyState by stateDelegate {
    private var routePrefix: String? = null

    // Delegate provider over the task's own journeyStateService, used by subclasses to create route-scoped
    // nullable delegates.
    protected val delegateProvider = JourneyStateDelegateProvider(journeyStateService)

    // Route-only late binding - the sole value the TaskInitialiser supplies at build time.
    override fun bindRoute(routePrefix: String?) {
        this.routePrefix = routePrefix
    }

    protected fun scopedKey(key: String) = routePrefix?.let { "$it/$key" } ?: key

    // Only the single-key step-data accessors are route-scoped here. Every other JourneyState member is delegated
    // unchanged via `by stateDelegate`, so it operates on the whole (unscoped) journey. In particular
    // getSubmittedStepData() returns the ENTIRE journey's step data with fully-qualified keys (route-prefixed and
    // bare, all mixed), NOT just this instance's data - so it must not be used to enumerate a single routed
    // instance's data. (getStepData in AbstractJourneyState folds over that same whole-journey map, but is
    // overridden below to look up the route-scoped key.)
    override fun getStepData(key: String): FormData? = stateDelegate.getStepData(scopedKey(key))

    override fun addStepData(
        key: String,
        value: FormData,
    ) = stateDelegate.addStepData(scopedKey(key), value)

    override fun clearStepData(key: String) = stateDelegate.clearStepData(scopedKey(key))
}
