package uk.gov.communities.prsdb.webapp.journeys.example.tasks

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.example.EpcJourneyState
import uk.gov.communities.prsdb.webapp.journeys.example.steps.CheckEpcStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcSearchResult
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcStatus
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@PrsdbWebComponent
@Scope("prototype")
class EpcTask : Task<EpcJourneyState>() {
    override fun makeSubJourney(state: EpcJourneyState) =
        subJourney(state) {
            step(journey.epcQuestion) {
                routeSegment("has-epc")
                nextStep { mode ->
                    when (mode) {
                        EpcStatus.AUTOMATCHED -> journey.checkAutomatchedEpc
                        EpcStatus.NOT_AUTOMATCHED -> journey.searchForEpc
                        EpcStatus.NO_EPC -> exitStep
                    }
                }
            }
            step<YesOrNo, CheckEpcStepConfig>(journey.checkAutomatchedEpc) {
                routeSegment("check-automatched-epc")
                parents { journey.epcQuestion.hasOutcome(EpcStatus.AUTOMATCHED) }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> exitStep
                        YesOrNo.NO -> journey.searchForEpc
                    }
                }
                stepSpecificInitialisation {
                    usingEpc { automatchedEpc }
                }
            }
            step(journey.searchForEpc) {
                routeSegment("search-for-epc")
                parents {
                    OrParents(
                        journey.epcQuestion.hasOutcome(EpcStatus.NOT_AUTOMATCHED),
                        journey.checkAutomatchedEpc.hasOutcome(YesOrNo.NO),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        EpcSearchResult.FOUND -> journey.checkSearchedEpc
                        EpcSearchResult.SUPERSEDED -> journey.epcSuperseded
                        EpcSearchResult.NOT_FOUND -> journey.epcNotFound
                    }
                }
            }
            step(journey.epcSuperseded) {
                routeSegment("superseded-epc")
                parents { journey.searchForEpc.hasOutcome(EpcSearchResult.SUPERSEDED) }
                nextStep { journey.checkSearchedEpc }
            }
            step<YesOrNo, CheckEpcStepConfig>(journey.checkSearchedEpc) {
                routeSegment("check-found-epc")
                parents {
                    OrParents(
                        journey.searchForEpc.hasOutcome(EpcSearchResult.FOUND),
                        journey.epcSuperseded.hasOutcome(Complete.COMPLETE),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> exitStep
                        YesOrNo.NO -> journey.searchForEpc
                    }
                }
                stepSpecificInitialisation {
                    usingEpc { searchedEpc }
                }
            }
            step(journey.epcNotFound) {
                routeSegment("epc-not-found")
                parents { journey.searchForEpc.hasOutcome(EpcSearchResult.NOT_FOUND) }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.epcQuestion.hasOutcome(EpcStatus.NO_EPC),
                        journey.checkAutomatchedEpc.hasOutcome(YesOrNo.YES),
                        journey.checkSearchedEpc.hasOutcome(YesOrNo.YES),
                        journey.epcNotFound.hasOutcome(Complete.COMPLETE),
                    )
                }
            }
        }
}
