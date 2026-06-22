package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordRegistrationCyaStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.NonEnglandOrWalesAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.IdentityTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LandlordRegistrationAddressTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LandlordRegistrationTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState

interface LandlordRegistrationState :
    IdentityState,
    AddressState,
    CheckYourAnswersJourneyState {
    val landlordRegistrationTask: LandlordRegistrationTask
    val privacyNoticeStep: PrivacyNoticeStep
    val identityTask: IdentityTask
    val emailStep: EmailStep
    val phoneNumberStep: PhoneNumberStep
    val countryOfResidenceStep: CountryOfResidenceStep
    val nonEnglandOrWalesAddressStep: NonEnglandOrWalesAddressStep
    val addressTask: LandlordRegistrationAddressTask
    override val finishCyaStep: FinishCyaJourneyStep
    override val cyaStep: LandlordRegistrationCyaStep
}
