package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_UPLOAD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState

@JourneyFrameworkComponent
class GasSafetyTask : Task<GasSafetyState>() {
    override fun makeSubJourney(state: GasSafetyState) =
        subJourney(state) {
            step(journey.uploadGasSafetyStep) {
                routeSegment(GAS_SAFETY_UPLOAD_PATH_SEGMENT)
                nextStep { exitStep }
            }
        }
}
