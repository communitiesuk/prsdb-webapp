package uk.gov.communities.prsdb.webapp.journeys

// A task that needs no typed access to the enclosing state: the TDependencies = Nothing case of Task. It retains
// all route-scoping/self-state behaviour from Task. requiresDependencies is false so it can be mounted with a bare
// task(...) { } (no withDependencies { } needed).
abstract class TaskWithoutDependencies<TState : JourneyState>(
    journeyStateService: JourneyStateService,
) : Task<TState, Nothing>(journeyStateService) {
    override val requiresDependencies: Boolean = false
}
