package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordRegistrationCyaStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeChangeRedirectStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.IdentityTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.IndividualLandlordLocationTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgLandlordRegistrationTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState

interface LandlordRegistrationState :
    CheckYourAnswersJourneyState {
    val emailStep: EmailStep
    val phoneNumberStep: PhoneNumberStep
    val individualLandlordLocationTask: IndividualLandlordLocationTask
    val orgLandlordRegistrationTask: OrgLandlordRegistrationTask
    val landlordTypeStep: LandlordTypeStep
    val landlordTypeChangeRedirectStep: LandlordTypeChangeRedirectStep
    val privacyNoticeStep: PrivacyNoticeStep
    val identityTask: IdentityTask
    override val finishCyaStep: FinishCyaJourneyStep
    override val cyaStep: LandlordRegistrationCyaStep
}
