package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDependencies

class ReactivationGasSafetyDependencies(
    private val state: PropertyReactivationJourneyState,
) : GasSafetyDependencies {
    override val isOccupied: Boolean
        get() = state.isOccupied

    override val allowProvideCertificateLaterRoute: Boolean = false

    override val propertyOwnershipId: Long
        get() = state.propertyOwnershipId
}
