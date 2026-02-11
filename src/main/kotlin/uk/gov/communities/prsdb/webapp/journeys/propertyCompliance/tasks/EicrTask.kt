package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState

@JourneyFrameworkComponent
class EicrTask : Task<EicrState>() {
    // TODO PDJB-467 - configure this task
    override fun makeSubJourney(state: EicrState) =
        subJourney(state) {
            exitStep
        }
}
