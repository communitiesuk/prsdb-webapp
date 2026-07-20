package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyContainerState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep

@JourneyFrameworkComponent
class ElectricalSafetyTask : Task<ElectricalSafetyContainerState>() {
    override fun makeSubJourney(state: ElectricalSafetyContainerState) =
        subJourney(state) {
            duplicableTask(journey.electricalSafetyDetailsTask) {
                withDependencies { journey }
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
