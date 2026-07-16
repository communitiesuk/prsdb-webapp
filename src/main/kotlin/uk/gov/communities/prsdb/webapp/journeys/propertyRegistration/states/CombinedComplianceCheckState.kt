package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask

interface CombinedComplianceCheckState : JourneyState {
    val isOccupied: Boolean
    val gasSafetyDetailsTask: GasSafetyDetailsTask
    val electricalSafetyDetailsTask: ElectricalSafetyDetailsTask
    val epcDetailsTask: EpcDetailsTask
}
