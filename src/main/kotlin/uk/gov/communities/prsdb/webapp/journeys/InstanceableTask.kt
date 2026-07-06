package uk.gov.communities.prsdb.webapp.journeys

// A task that can be added to a journey more than once, each instance under its own route. The DSL's
// `instancedTask` builds the task against the scoped state returned here, so each instance's steps and data are
// isolated without the journey state needing per-instance fields. `delegate` is the real journey state that the
// scoped state stores its data against, and `routeSegment` is the route the instance is added under.
interface InstanceableTask<out TState : JourneyState> {
    fun createScopedState(
        delegate: JourneyState,
        routeSegment: String,
    ): TState
}
