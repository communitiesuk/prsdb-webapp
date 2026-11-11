package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDependencies

class ReactivationElectricalSafetyDependencies(
    private val state: PropertyReactivationJourneyState,
) : ElectricalSafetyDependencies {
    override val isOccupied: Boolean
        get() = state.isOccupied

    override val allowProvideCertificateLaterRoute: Boolean = false

    override val propertyOwnershipId: Long
        get() = state.propertyOwnershipId
}
