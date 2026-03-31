package uk.gov.communities.prsdb.webapp.journeys.joinProperty.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.PrnSearchState
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyByPrnStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PrnNotFoundStep

@JourneyFrameworkComponent
class PrnSearchTask : Task<PrnSearchState>() {
    override fun makeSubJourney(state: PrnSearchState) =
        subJourney(state) {
            // TODO: PDJB-277 - Connect from FindProperty page link
            step(journey.findPropertyByPrnStep) {
                routeSegment(FindPropertyByPrnStep.ROUTE_SEGMENT)
                nextStep { journey.prnNotFoundStep }
            }
            // TODO: PDJB-279 - Connect when PRN not found
            step(journey.prnNotFoundStep) {
                routeSegment(PrnNotFoundStep.ROUTE_SEGMENT)
                parents { journey.findPropertyByPrnStep.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.prnNotFoundStep.isComplete() }
            }
        }
}
