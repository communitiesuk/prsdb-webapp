package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseholdsAndTenantsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentFrequencyAndAmountTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentIncludesBillsTask

interface OccupationState :
    JourneyState,
    HouseholdsAndTenantsState,
    BedroomsState,
    RentIncludesBillsState,
    FurnishedStatusState,
    RentFrequencyAndAmountState {
    val occupied: OccupiedStep
    val householdsAndTenantsTask: HouseholdsAndTenantsTask
    val rentIncludesBillsTask: RentIncludesBillsTask
    val rentFrequencyAndAmountTask: RentFrequencyAndAmountTask
    var cachedOccupied: Boolean?
}
