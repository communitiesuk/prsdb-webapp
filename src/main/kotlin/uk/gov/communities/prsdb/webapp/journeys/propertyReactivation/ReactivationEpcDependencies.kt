package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDependencies

class ReactivationEpcDependencies(
    private val state: PropertyReactivationJourneyState,
) : EpcDependencies {
    override val isOccupied: Boolean
        get() = state.isOccupied

    override val uprn: Long?
        get() = state.uprn

    override val allowProvideCertificateLaterRoute: Boolean = false
}
