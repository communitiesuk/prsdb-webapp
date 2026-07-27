package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

interface CombinedComplianceCheckState :
    ElectricalSafetyState,
    EpcState {
    val gasSafetyTask: GasSafetyState
}
