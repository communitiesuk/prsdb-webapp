package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep

@JourneyFrameworkComponent
class ElectricalSafetyTask : Task<ElectricalSafetyState>() {
    override fun makeSubJourney(state: ElectricalSafetyState) =
        subJourney(state) {
            task(journey.electricalSafetyDetailsTask) {
                nextStep { journey.checkElectricalSafetyAnswersStep }
                savable()
            }
            step(journey.checkElectricalSafetyAnswersStep) {
                routeSegment(CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT)
                parents { journey.electricalSafetyDetailsTask.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.checkElectricalSafetyAnswersStep.isComplete() }
            }
        }
}
