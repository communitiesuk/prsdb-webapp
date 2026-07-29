package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

interface CombinedComplianceCheckState : EpcState {
    override val isOccupied: Boolean
    val gasSafetyTask: GasSafetyState
    val electricalSafetyTask: ElectricalSafetyState
}
