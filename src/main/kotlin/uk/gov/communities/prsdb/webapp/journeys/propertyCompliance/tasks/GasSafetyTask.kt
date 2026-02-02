package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class GasSafetyTask : Task<GasSafetyState>() {
    override fun makeSubJourney(state: GasSafetyState) =
        subJourney(state) {
            step(journey.gasSafetyEngineerNumberStep) {
                routeSegment(GasSafetyEngineerNumberStep.ROUTE_SEGMENT)
                nextStep { state.gasSafetyCertificateUploadStep }
                savable()
            }
            step(journey.gasSafetyCertificateUploadStep) {
                routeSegment(GasSafetyCertificateUploadStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyEngineerNumberStep.hasOutcome(Complete.COMPLETE) }
                nextStep { journey.gasSafetyUploadConfirmationStep }
                savable()
            }
            step(journey.gasSafetyUploadConfirmationStep) {
                routeSegment(GasSafetyUploadConfirmationStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyCertificateUploadStep.hasOutcome(Complete.COMPLETE) }
                noNextDestination()
                savable()
            }
            exitStep {
                parents {
                    journey.gasSafetyUploadConfirmationStep.hasOutcome(Complete.COMPLETE)
                }
            }
        }
}
