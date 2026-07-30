package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTaskWithDependencies
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep

@JourneyFrameworkComponent("propertyRegistrationGasSafetyTask")
class GasSafetyTask(
    journeyStateService: JourneyStateService,
    override val gasSafetyDetailsTask: GasSafetyDetailsTask,
    override val checkGasSafetyAnswersStep: CheckGasSafetyAnswersStep,
) : DuplicableTaskWithDependencies<GasSafetyState, GasSafetyDependencies>(journeyStateService),
    GasSafetyState {
    override fun makeSubJourney(state: GasSafetyState) =
        subJourney(state) {
            duplicableTask(journey.gasSafetyDetailsTask) {
                withDependencies { dependencies }
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

    override val taskState: GasSafetyState
        get() = this
}

interface GasSafetyDependencies {
    val isOccupied: Boolean
    val allowProvideCertificateLaterRoute: Boolean
}
