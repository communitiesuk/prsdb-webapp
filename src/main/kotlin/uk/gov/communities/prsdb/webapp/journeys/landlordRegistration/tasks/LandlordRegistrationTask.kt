package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.NonEnglandOrWalesAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep

@JourneyFrameworkComponent
class LandlordRegistrationTask : Task<LandlordRegistrationState>() {
    override fun makeSubJourney(state: LandlordRegistrationState) =
        subJourney(state) {
            step(journey.privacyNoticeStep) {
                routeSegment(PrivacyNoticeStep.ROUTE_SEGMENT)
                nextStep { journey.identityTask.firstStep }
            }
            task(journey.identityTask) {
                parents { journey.privacyNoticeStep.isComplete() }
                nextStep { journey.emailStep }
            }
            step(journey.emailStep) {
                routeSegment(EmailStep.ROUTE_SEGMENT)
                parents { journey.identityTask.isComplete() }
                nextStep { journey.phoneNumberStep }
            }
            step(journey.phoneNumberStep) {
                routeSegment(PhoneNumberStep.ROUTE_SEGMENT)
                parents { journey.emailStep.isComplete() }
                nextStep { journey.countryOfResidenceStep }
            }
            step(journey.countryOfResidenceStep) {
                routeSegment(CountryOfResidenceStep.ROUTE_SEGMENT)
                parents { journey.phoneNumberStep.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        CountryOfResidenceMode.ENGLAND_OR_WALES -> journey.addressTask.firstStep
                        CountryOfResidenceMode.NON_ENGLAND_OR_WALES -> journey.nonEnglandOrWalesAddressStep
                    }
                }
            }
            step(journey.nonEnglandOrWalesAddressStep) {
                routeSegment(NonEnglandOrWalesAddressStep.ROUTE_SEGMENT)
                parents { journey.countryOfResidenceStep.hasOutcome(CountryOfResidenceMode.NON_ENGLAND_OR_WALES) }
                noNextDestination()
            }
            task(journey.addressTask) {
                parents { journey.countryOfResidenceStep.hasOutcome(CountryOfResidenceMode.ENGLAND_OR_WALES) }
                nextStep { journey.cyaStep }
            }
            step(journey.cyaStep) {
                routeSegment(AbstractCheckYourAnswersStep.ROUTE_SEGMENT)
                parents { journey.addressTask.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.cyaStep.isComplete() }
            }
        }
}
