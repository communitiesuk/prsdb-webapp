package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcContainerState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep

@JourneyFrameworkComponent("propertyRegistrationEpcTask")
class EpcTask : Task<EpcContainerState>() {
    override fun makeSubJourney(state: EpcContainerState) =
        subJourney(state) {
            taskStatus {
                when {
                    exitStep.isStepReachable -> TaskStatus.COMPLETED
                    journey.epcDetailsTask.checkUprnMatchedEpcStep.outcome != null -> TaskStatus.IN_PROGRESS
                    journey.epcDetailsTask.hasEpcStep.outcome != null -> TaskStatus.IN_PROGRESS
                    journey.epcDetailsTask.startEpcStep.isStepReachable -> TaskStatus.NOT_STARTED
                    else -> TaskStatus.CANNOT_START
                }
            }
            duplicableTask(journey.epcDetailsTask) {
                withDependencies { journey }
                nextStep { journey.checkEpcAnswersStep }
                savable()
            }
            step(journey.checkEpcAnswersStep) {
                routeSegment(CheckEpcAnswersStep.ROUTE_SEGMENT)
                parents { journey.epcDetailsTask.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.checkEpcAnswersStep.isComplete() }
            }
        }
}
