package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckAutomatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckAutomatchedEpcStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcSearchResult
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcStatusMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.SearchForEpcStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class EpcTask : Task<EpcState>() {
    override fun makeSubJourney(state: EpcState) =
        subJourney(state) {
            step(journey.epcQuestionStep) {
                routeSegment(EpcQuestionStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        EpcStatusMode.AUTOMATCHED -> journey.checkAutomatchedEpcStep
                        EpcStatusMode.NOT_AUTOMATCHED -> journey.searchForEpcStep
                        EpcStatusMode.NO_EPC -> exitStep
                    }
                }
                savable()
            }
            step<CheckMatchedEpcMode, CheckAutomatchedEpcStepConfig>(journey.checkAutomatchedEpcStep) {
                routeSegment(CheckAutomatchedEpcStep.ROUTE_SEGMENT)
                parents { journey.epcQuestionStep.hasOutcome(EpcStatusMode.AUTOMATCHED) }
                nextStep { mode ->
                    when (mode) {
                        CheckMatchedEpcMode.EPC_CORRECT -> exitStep
                        CheckMatchedEpcMode.EPC_INCORRECT -> journey.searchForEpcStep
                    }
                }
                stepSpecificInitialisation {
                    usingEpc { automatchedEpc }
                }
                savable()
            }
            step(journey.searchForEpcStep) {
                routeSegment(SearchForEpcStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.epcQuestionStep.hasOutcome(EpcStatusMode.NOT_AUTOMATCHED),
                        journey.checkAutomatchedEpcStep.hasOutcome(CheckMatchedEpcMode.EPC_INCORRECT),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        EpcSearchResult.FOUND -> journey.checkSearchedEpcStep
                        EpcSearchResult.SUPERSEDED -> journey.epcSupersededStep
                        EpcSearchResult.NOT_FOUND -> journey.epcNotFoundStep
                    }
                }
                savable()
            }
            step(journey.epcSupersededStep) {
                routeSegment(EpcSupersededStep.ROUTE_SEGMENT)
                parents { journey.searchForEpcStep.hasOutcome(EpcSearchResult.SUPERSEDED) }
                nextStep { journey.checkSearchedEpcStep }
                savable()
            }
            step<CheckMatchedEpcMode, CheckMatchedEpcStepConfig>(journey.checkSearchedEpcStep) {
                routeSegment(CheckMatchedEpcStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.searchForEpcStep.hasOutcome(EpcSearchResult.FOUND),
                        journey.epcSupersededStep.hasOutcome(Complete.COMPLETE),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        CheckMatchedEpcMode.EPC_CORRECT -> exitStep
                        CheckMatchedEpcMode.EPC_INCORRECT -> journey.searchForEpcStep
                    }
                }
                stepSpecificInitialisation {
                    usingEpc { searchedEpc }
                }
                savable()
            }
            step(journey.epcNotFoundStep) {
                routeSegment(EpcNotFoundStep.ROUTE_SEGMENT)
                parents { journey.searchForEpcStep.hasOutcome(EpcSearchResult.NOT_FOUND) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.epcQuestionStep.hasOutcome(EpcStatusMode.NO_EPC),
                        journey.checkAutomatchedEpcStep.hasOutcome(CheckMatchedEpcMode.EPC_CORRECT),
                        journey.checkSearchedEpcStep.hasOutcome(CheckMatchedEpcMode.EPC_CORRECT),
                        journey.epcNotFoundStep.hasOutcome(Complete.COMPLETE),
                    )
                }
            }
        }
}
