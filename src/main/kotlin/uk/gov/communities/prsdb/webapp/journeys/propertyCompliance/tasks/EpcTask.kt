package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcStatus
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState

@JourneyFrameworkComponent
class EpcTask : Task<EpcState>() {
    override fun makeSubJourney(state: EpcState) =
        // TODO PDJB-467: Configure this task - some steps may exist in ExampleEpcTask but should be checked to make sure they are correct
        subJourney(state) {
            step(journey.epcQuestionStep) {
                routeSegment("has-epc")
                nextStep { exitStep }
                /*nextStep { mode ->
                    when (mode) {
                        EpcStatus.AUTOMATCHED -> journey.checkAutomatchedEpc
                        EpcStatus.NOT_AUTOMATCHED -> journey.searchForEpc
                        EpcStatus.NO_EPC -> exitStep
                    }
                }*/
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.epcQuestionStep.hasOutcome(EpcStatus.NO_EPC),
                      /*  journey.checkAutomatchedEpc.hasOutcome(YesOrNo.YES),
                        journey.checkSearchedEpc.hasOutcome(YesOrNo.YES),
                        journey.epcNotFound.hasOutcome(Complete.COMPLETE),*/
                    )
                }
            }
        }
}
