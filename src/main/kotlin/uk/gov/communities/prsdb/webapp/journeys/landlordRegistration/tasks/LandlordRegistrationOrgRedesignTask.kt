package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationOrgRedesignState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep

@JourneyFrameworkComponent
class LandlordRegistrationOrgRedesignTask : Task<LandlordRegistrationOrgRedesignState>() {
    override fun makeSubJourney(state: LandlordRegistrationOrgRedesignState) =
        subJourney(state) {
            step(journey.privacyNoticeStep) {
                routeSegment(PrivacyNoticeStep.ROUTE_SEGMENT)
                nextStep { journey.identityTask.firstStep }
            }
            task(journey.identityTask) {
                parents { journey.privacyNoticeStep.isComplete() }
                nextStep { journey.landlordTypeStep }
            }
            step(journey.landlordTypeStep) {
                routeSegment(LandlordTypeStep.ROUTE_SEGMENT)
                parents { journey.identityTask.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        LandlordTypeMode.INDIVIDUAL -> journey.landlordRegistrationForNotOrgLandlordTask.firstStep
                        LandlordTypeMode.ORGANISATION -> journey.landlordRegistrationForOrgLandlordTask.firstStep
                    }
                }
            }
            task(journey.landlordRegistrationForNotOrgLandlordTask) {
                parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.INDIVIDUAL) }
                nextStep { exitStep }
            }
            task(journey.landlordRegistrationForOrgLandlordTask) {
                parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.ORGANISATION) }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    journey.landlordRegistrationForNotOrgLandlordTask.isComplete()
                    journey.landlordRegistrationForOrgLandlordTask.isComplete()
                }
            }
        }
}
