package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.CorrespondenceAddressTask

interface CorrespondenceState : JourneyState {
    val correspondenceEmailStep: CorrespondenceEmailStep
    val addressTask: CorrespondenceAddressTask
}

// Skeleton "Who the council should contact" task (PDJB-1589): a placeholder correspondence email step
// (TODO PDJB-1590) followed by the reused postal-address flow (CorrespondenceAddressTask).
@JourneyFrameworkComponent
class CorrespondenceTask(
    journeyStateService: JourneyStateService,
    override val correspondenceEmailStep: CorrespondenceEmailStep,
    override val addressTask: CorrespondenceAddressTask,
) : TaskWithoutDependencies<CorrespondenceState>(journeyStateService),
    CorrespondenceState {
    override val taskState get() = this

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
