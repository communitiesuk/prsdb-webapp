package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState

interface CombinedComplianceCheckState : JourneyState {
    val isOccupied: Boolean
    val gasSafetyTask: GasSafetyState
    val electricalSafetyTask: ElectricalSafetyState
    val epcTask: EpcState
}
