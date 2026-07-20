package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.NonEnglandOrWalesAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.AddressTask

interface IndividualLandlordRegistrationState : JourneyState {
    val emailStep: EmailStep
    val phoneNumberStep: PhoneNumberStep
    val countryOfResidenceStep: CountryOfResidenceStep
    val nonEnglandOrWalesAddressStep: NonEnglandOrWalesAddressStep
    val addressTask: AddressTask
}
