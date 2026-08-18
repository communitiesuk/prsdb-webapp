package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.NonEnglandOrWalesAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.AddressTask

interface IndividualLandlordLocationState : JourneyState {
    val countryOfResidenceStep: CountryOfResidenceStep
    val nonEnglandOrWalesAddressStep: NonEnglandOrWalesAddressStep
    val addressTask: AddressTask
}
