package uk.gov.communities.prsdb.webapp.journeys.example.tasks

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.subJourney
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
        step("has-epc", journey.epcQuestion) {
            parents { entryPoint }
            nextStep { mode ->
                when (mode) {
                    EpcStatus.AUTOMATCHED -> journey.checkAutomatchedEpc
                    EpcStatus.NOT_AUTOMATCHED -> journey.searchForEpc
                    EpcStatus.NO_EPC -> notionalExitStep
                }
            }
        }
        step<YesOrNo, CheckEpcStepConfig>("check-automatched-epc", journey.checkAutomatchedEpc) {
            parents { journey.epcQuestion.hasOutcome(EpcStatus.AUTOMATCHED) }
            nextStep { mode ->
                when (mode) {
                    YesOrNo.YES -> notionalExitStep
                    YesOrNo.NO -> journey.searchForEpc
                }
            }
            stepSpecificInitialisation {
                usingEpc { automatchedEpc }
            }
        }
        step("search-for-epc", journey.searchForEpc) {
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
        step("superseded-epc", journey.epcSuperseded) {
            parents { journey.searchForEpc.hasOutcome(EpcSearchResult.SUPERSEDED) }
            nextStep { journey.checkSearchedEpc }
        }
        step<YesOrNo, CheckEpcStepConfig>("check-found-epc", journey.checkSearchedEpc) {
            parents {
                OrParents(
                    journey.searchForEpc.hasOutcome(EpcSearchResult.FOUND),
                    journey.epcSuperseded.hasOutcome(Complete.COMPLETE),
                )
            }
            nextStep { mode ->
                when (mode) {
                    YesOrNo.YES -> notionalExitStep
                    YesOrNo.NO -> journey.searchForEpc
                }
            }
            stepSpecificInitialisation {
                usingEpc { searchedEpc }
            }
        }
        step("epc-not-found", journey.epcNotFound) {
            parents { journey.searchForEpc.hasOutcome(EpcSearchResult.NOT_FOUND) }
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

    override fun firstStepInTask(state: EpcJourneyState): JourneyStep<*, *, EpcJourneyState> = state.epcQuestion
}
