package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask

interface CombinedComplianceCheckState :
    ElectricalSafetyState,
    EpcState {
    val gasSafetyDetailsTask: GasSafetyDetailsTask
}
