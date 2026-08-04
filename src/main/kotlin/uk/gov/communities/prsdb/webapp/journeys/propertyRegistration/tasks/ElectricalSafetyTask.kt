package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep

@JourneyFrameworkComponent("propertyRegistrationElectricalSafetyTask")
class ElectricalSafetyTask(
    journeyStateService: JourneyStateService,
    override val electricalSafetyDetailsTask: ElectricalSafetyDetailsTask,
    override val checkElectricalSafetyAnswersStep: CheckElectricalSafetyAnswersStep,
) : Task<ElectricalSafetyState, ElectricalSafetyDependencies>(journeyStateService),
    ElectricalSafetyState {
    override fun makeSubJourney(state: ElectricalSafetyState) =
        subJourney(state) {
            task(journey.electricalSafetyDetailsTask) {
                withDependencies { dependencies }
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

    override val taskState: ElectricalSafetyState
        get() = this
}

interface ElectricalSafetyDependencies {
    val isOccupied: Boolean
    val allowProvideCertificateLaterRoute: Boolean
}
