package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseholdsAndTenantsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentFrequencyAndAmountTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentIncludesBillsTask

interface TenancyDetailsState :
    JourneyState,
    HouseholdsAndTenantsState,
    RentIncludesBillsState,
    FurnishedStatusState {
    val householdsAndTenantsTask: HouseholdsAndTenantsTask
    val rentIncludesBillsTask: RentIncludesBillsTask
    val rentFrequencyAndAmountTask: RentFrequencyAndAmountTask
}
