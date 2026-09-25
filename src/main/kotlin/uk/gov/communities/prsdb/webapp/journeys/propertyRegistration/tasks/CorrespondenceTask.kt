package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CorrespondenceEmailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.CorrespondenceAddressTask

interface CorrespondenceState : CorrespondenceEmailState {
    val addressTask: CorrespondenceAddressTask
}

@JourneyFrameworkComponent
class CorrespondenceTask(
    journeyStateService: JourneyStateService,
    override val correspondenceEmailStep: CorrespondenceEmailStep,
    override val addressTask: CorrespondenceAddressTask,
) : Task<CorrespondenceState, CorrespondenceDependencies>(journeyStateService),
    CorrespondenceState {
    override val taskState get() = this

    override val loggedInLandlordEmailAtStartOfJourney: String?
        get() = dependencies.loggedInLandlordEmailAtStartOfJourney

    override fun makeSubJourney(state: CorrespondenceState) =
        subJourney(state) {
            step(journey.correspondenceEmailStep) {
                routeSegment(CorrespondenceEmailStep.ROUTE_SEGMENT)
                nextStep { journey.addressTask.firstStep }
            }
            task(journey.addressTask, CorrespondenceAddressTask.ROUTE_SEGMENT) {
                parents { journey.correspondenceEmailStep.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.addressTask.isComplete() }
            }
        }
}

interface CorrespondenceDependencies {
    val loggedInLandlordEmailAtStartOfJourney: String?
}
