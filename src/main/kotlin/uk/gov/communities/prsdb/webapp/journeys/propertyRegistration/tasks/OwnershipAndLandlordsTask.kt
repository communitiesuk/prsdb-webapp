package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OwnershipAndLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep

@JourneyFrameworkComponent
class OwnershipAndLandlordsTask : Task<OwnershipAndLandlordsState>() {
    override fun makeSubJourney(state: OwnershipAndLandlordsState) =
        subJourney(state) {
            step(journey.ownershipTypeStep) {
                routeSegment(OwnershipTypeStep.ROUTE_SEGMENT)
                nextStep { journey.jointLandlordsTask.firstStep }
                savable()
            }
            duplicableTask(journey.jointLandlordsTask) {
                withDependencies { journey }
                parents { journey.ownershipTypeStep.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.jointLandlordsTask.isComplete() }
            }
        }
}
