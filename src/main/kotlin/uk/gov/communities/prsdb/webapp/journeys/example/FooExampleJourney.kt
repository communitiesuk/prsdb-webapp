package uk.gov.communities.prsdb.webapp.journeys.example

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourney
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.always
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.example.steps.CheckEpcStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcSearchResult
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcStatus
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.FooCheckAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.FooTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.SearchEpcStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

@Scope("prototype")
@PrsdbWebService
class FooExampleJourney(
    private val taskListStep: FooTaskListStep,
    override val occupied: OccupiedStep,
    override val households: HouseholdStep,
    override val tenants: TenantsStep,
    override val epcQuestion: EpcQuestionStep,
    override val searchForEpc: SearchEpcStep,
    override val epcNotFound: EpcNotFoundStep,
    override val epcSuperseded: EpcSupersededStep,
    override val checkAutomatchedEpc: CheckEpcStep,
    override val checkSearchedEpc: CheckEpcStep,
    private val fooCheckYourAnswersStep: FooCheckAnswersStep,
    private val journeyStateService: JourneyStateService,
) : AbstractJourney(journeyStateService),
    FooJourneyState {
    final override fun buildJourneySteps(journeyId: String): Map<String, StepLifecycleOrchestrator> {
        initialise(journeyId)

        return journey(this) {
            step("task-list", taskListStep) {
                redirectToUrl { "task-list" }
            }
            step("occupied", occupied) {
                parents { taskListStep.always() }
                redirectToStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> households
                        YesOrNo.NO -> fooCheckYourAnswersStep
                    }
                }
            }
            step("households", households) {
                parents { occupied.hasOutcome(YesOrNo.YES) }
                redirectToStep { tenants }
            }
            step("tenants", tenants) {
                parents { households.hasOutcome(Complete.COMPLETE) }
                redirectToStep { fooCheckYourAnswersStep }
            }
            step("has-epc", epcQuestion) {
                parents { taskListStep.always() }
                redirectToStep { mode ->
                    when (mode) {
                        EpcStatus.AUTOMATCHED -> checkAutomatchedEpc
                        EpcStatus.NOT_AUTOMATCHED -> searchForEpc
                        EpcStatus.NO_EPC -> fooCheckYourAnswersStep
                    }
                }
            }
            step("check-automatched-epc", checkAutomatchedEpc) {
                parents { epcQuestion.hasOutcome(EpcStatus.AUTOMATCHED) }
                redirectToStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> fooCheckYourAnswersStep
                        YesOrNo.NO -> searchForEpc
                    }
                }
                stepSpecificInitialisation {
                    usingEpc { automatchedEpc }
                }
            }
            step("search-for-epc", searchForEpc) {
                parents { OrParents(epcQuestion.hasOutcome(EpcStatus.NOT_AUTOMATCHED), checkAutomatchedEpc.hasOutcome(YesOrNo.NO)) }
                redirectToStep { mode ->
                    when (mode) {
                        EpcSearchResult.FOUND -> checkSearchedEpc
                        EpcSearchResult.SUPERSEDED -> epcSuperseded
                        EpcSearchResult.NOT_FOUND -> epcNotFound
                    }
                }
            }
            step("superseded-epc", epcSuperseded) {
                parents { searchForEpc.hasOutcome(EpcSearchResult.SUPERSEDED) }
                redirectToStep { checkSearchedEpc }
            }
            step("check-found-epc", checkSearchedEpc) {
                parents {
                    OrParents(
                        searchForEpc.hasOutcome(EpcSearchResult.FOUND),
                        epcSuperseded.hasOutcome(Complete.COMPLETE),
                    )
                }
                redirectToStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> fooCheckYourAnswersStep
                        YesOrNo.NO -> searchForEpc
                    }
                }
                stepSpecificInitialisation {
                    usingEpc { searchedEpc }
                }
            }
            step("epc-not-found", epcNotFound) {
                parents { searchForEpc.hasOutcome(EpcSearchResult.NOT_FOUND) }
                redirectToStep { fooCheckYourAnswersStep }
            }
            step("check-your-answers", fooCheckYourAnswersStep) {
                parents {
                    AndParents(
                        OrParents(
                            occupied.hasOutcome(YesOrNo.NO),
                            tenants.hasOutcome(Complete.COMPLETE),
                        ),
                        OrParents(
                            epcQuestion.hasOutcome(EpcStatus.NO_EPC),
                            checkAutomatchedEpc.hasOutcome(YesOrNo.YES),
                            checkSearchedEpc.hasOutcome(YesOrNo.YES),
                            epcNotFound.hasOutcome(Complete.COMPLETE),
                        ),
                    )
                }
                redirectToUrl { "/" }
            }
        }
    }

    final fun journeyStateInitialisation(propertyId: Long) {
        journeyStateService.initialise(propertyId.toString())
        journeyStateService.setValue("propertyId", Json.encodeToString(serializer(), propertyId))
    }

    override var automatchedEpc: EpcDataModel? by delegate("automatchedEpc", serializer())
    override var searchedEpc: EpcDataModel? by delegate("searchedEpc", serializer())
    override val propertyId: Long by compulsoryDelegate("propertyId", serializer())
}
