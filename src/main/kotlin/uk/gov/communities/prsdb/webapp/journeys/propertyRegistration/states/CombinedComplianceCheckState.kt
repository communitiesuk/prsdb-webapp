package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask

interface CombinedComplianceCheckState : EpcState {
    override val isOccupied: Boolean
    val gasSafetyDetailsTask: GasSafetyDetailsTask
    val electricalSafetyDetailsTask: ElectricalSafetyDetailsTask
}
