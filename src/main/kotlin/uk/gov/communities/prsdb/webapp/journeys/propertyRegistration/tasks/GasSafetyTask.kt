package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep

@JourneyFrameworkComponent("propertyRegistrationGasSafetyTask")
class GasSafetyTask : Task<GasSafetyState>() {
    override fun makeSubJourney(state: GasSafetyState) =
        subJourney(state) {
            task(journey.gasSafetyDetailsTask) {
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
