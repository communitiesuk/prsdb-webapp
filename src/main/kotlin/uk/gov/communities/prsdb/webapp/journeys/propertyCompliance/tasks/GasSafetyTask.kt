package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_UPLOAD_CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_UPLOAD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class GasSafetyTask : Task<GasSafetyState>() {
    override fun makeSubJourney(state: GasSafetyState) =
        subJourney(state) {
            step(journey.gasSafetyCertificateUploadStep) {
                routeSegment(GAS_SAFETY_UPLOAD_PATH_SEGMENT)
                nextStep { journey.gasSafetyUploadConfirmationStep }
                savable()
            }
            step(journey.gasSafetyUploadConfirmationStep) {
                routeSegment(GAS_SAFETY_UPLOAD_CONFIRMATION_PATH_SEGMENT)
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
