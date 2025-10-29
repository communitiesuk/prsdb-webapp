package uk.gov.communities.prsdb.webapp.journeys.example.tasks

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.builders.TaskBuilder.Companion.subJourney
import uk.gov.communities.prsdb.webapp.journeys.example.EpcJourneyState
import uk.gov.communities.prsdb.webapp.journeys.example.steps.CheckEpcStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcSearchResult
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcStatus
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome

@PrsdbWebComponent
@Scope("prototype")
class EpcTask : Task<Complete, EpcJourneyState>() {
    override fun makeSubJourney(
        state: EpcJourneyState,
        entryPoint: Parentage,
    ) = subJourney(state) {
        step("has-epc", task.epcQuestion) {
            parents { entryPoint }
            nextStep { mode ->
                when (mode) {
                    EpcStatus.AUTOMATCHED -> task.checkAutomatchedEpc
                    EpcStatus.NOT_AUTOMATCHED -> task.searchForEpc
                    EpcStatus.NO_EPC -> notionalExitStep
                }
            }
        }
        step<YesOrNo, CheckEpcStepConfig>("check-automatched-epc", task.checkAutomatchedEpc) {
            parents { task.epcQuestion.hasOutcome(EpcStatus.AUTOMATCHED) }
            nextStep { mode ->
                when (mode) {
                    YesOrNo.YES -> notionalExitStep
                    YesOrNo.NO -> task.searchForEpc
                }
            }
            stepSpecificInitialisation {
                usingEpc { automatchedEpc }
            }
        }
        step("search-for-epc", task.searchForEpc) {
            parents {
                OrParents(
                    task.epcQuestion.hasOutcome(EpcStatus.NOT_AUTOMATCHED),
                    task.checkAutomatchedEpc.hasOutcome(YesOrNo.NO),
                )
            }
            nextStep { mode ->
                when (mode) {
                    EpcSearchResult.FOUND -> task.checkSearchedEpc
                    EpcSearchResult.SUPERSEDED -> task.epcSuperseded
                    EpcSearchResult.NOT_FOUND -> task.epcNotFound
                }
            }
        }
        step("superseded-epc", task.epcSuperseded) {
            parents { task.searchForEpc.hasOutcome(EpcSearchResult.SUPERSEDED) }
            nextStep { task.checkSearchedEpc }
        }
        step<YesOrNo, CheckEpcStepConfig>("check-found-epc", task.checkSearchedEpc) {
            parents {
                OrParents(
                    task.searchForEpc.hasOutcome(EpcSearchResult.FOUND),
                    task.epcSuperseded.hasOutcome(Complete.COMPLETE),
                )
            }
            nextStep { mode ->
                when (mode) {
                    YesOrNo.YES -> notionalExitStep
                    YesOrNo.NO -> task.searchForEpc
                }
            }
            stepSpecificInitialisation {
                usingEpc { searchedEpc }
            }
        }
        step("epc-not-found", task.epcNotFound) {
            parents { task.searchForEpc.hasOutcome(EpcSearchResult.NOT_FOUND) }
            nextStep { notionalExitStep }
        }
    }

    override fun taskCompletionParentage(state: EpcJourneyState): Parentage =
        OrParents(
            state.epcQuestion.hasOutcome(EpcStatus.NO_EPC),
            state.checkAutomatchedEpc.hasOutcome(YesOrNo.YES),
            state.checkSearchedEpc.hasOutcome(YesOrNo.YES),
            state.epcNotFound.hasOutcome(Complete.COMPLETE),
        )
}
