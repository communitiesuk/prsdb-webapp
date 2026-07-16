package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyContainerState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep

@JourneyFrameworkComponent("propertyRegistrationGasSafetyTask")
class GasSafetyTask : Task<GasSafetyContainerState>() {
    override fun makeSubJourney(state: GasSafetyContainerState) =
        subJourney(state) {
            duplicableTask(journey.gasSafetyDetailsTask) {
                withDependencies { journey }
                nextStep { journey.checkGasSafetyAnswersStep }
                savable()
            }
            step(journey.checkGasSafetyAnswersStep) {
                routeSegment(CheckGasSafetyAnswersStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyDetailsTask.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.checkGasSafetyAnswersStep.isComplete() }
            }
        }
}
