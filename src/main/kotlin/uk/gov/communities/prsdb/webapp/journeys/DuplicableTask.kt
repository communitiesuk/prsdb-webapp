package uk.gov.communities.prsdb.webapp.journeys

// A duplicable task that needs no typed access to the enclosing state: the TDependencies = Nothing case of
// DuplicableTaskWithDependencies. It retains all route-scoping/self-state behaviour from that base (its own
// JourneyState via a self-made AbstractJourneyState delegate, a route-scoped delegateProvider, and route/key-registry
// late binding). requiresDependencies is false so it can be mounted with a bare duplicableTask(...) { } (no
// withDependencies { } needed).
abstract class DuplicableTask<TState : JourneyState>(
    journeyStateService: JourneyStateService,
) : DuplicableTaskWithDependencies<TState, Nothing>(journeyStateService) {
    override val requiresDependencies: Boolean = false
}
