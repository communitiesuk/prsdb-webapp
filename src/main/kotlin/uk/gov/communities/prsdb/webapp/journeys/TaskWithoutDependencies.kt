package uk.gov.communities.prsdb.webapp.journeys

abstract class TaskWithoutDependencies<TState : JourneyState>(
    journeyStateService: JourneyStateService,
) : Task<TState, Nothing>(journeyStateService) {
    override val requiresDependencies: Boolean = false
}
