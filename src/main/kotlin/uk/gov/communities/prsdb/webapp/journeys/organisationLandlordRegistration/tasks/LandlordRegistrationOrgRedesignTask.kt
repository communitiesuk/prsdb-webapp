package uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.LandlordRegistrationOrgRedesignState
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.LandlordTypeMode
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.LandlordTypeStep

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
                        LandlordTypeMode.INDIVIDUAL -> journey.landlordRegistrationNotOrgLandlordTask.firstStep
                        LandlordTypeMode.ORGANISATION -> journey.landlordRegistrationOrgLandlordTask.firstStep
                    }
                }
            }
            task(journey.landlordRegistrationNotOrgLandlordTask) {
                parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.INDIVIDUAL) }
                nextStep { exitStep }
            }
            task(journey.landlordRegistrationOrgLandlordTask) {
                parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.ORGANISATION) }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    journey.landlordRegistrationNotOrgLandlordTask.isComplete()
                    journey.landlordRegistrationOrgLandlordTask.isComplete()
                }
            }
        }
}
