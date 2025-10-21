package uk.gov.communities.prsdb.webapp.journeys.example

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateServiceFactory
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.always
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.example.steps.CheckEpcStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcNotFoundStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcQuestionStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcSearchResult
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcStatus
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcSupersededStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.FooCheckAnswersStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.FooTaskListStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.HouseholdStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.OccupiedStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.SearchEpcStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.TenantsStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel

@PrsdbWebService
class FooExampleJourneyFactory(
    private val taskListStepFactory: ObjectFactory<FooTaskListStepConfig>,
    val occupiedFactory: ObjectFactory<OccupiedStepConfig>,
    val householdsFactory: ObjectFactory<HouseholdStepConfig>,
    val tenantsFactory: ObjectFactory<TenantsStepConfig>,
    val epcQuestionFactory: ObjectFactory<EpcQuestionStepConfig>,
    val searchForEpcFactory: ObjectFactory<SearchEpcStepConfig>,
    val epcNotFoundFactory: ObjectFactory<EpcNotFoundStepConfig>,
    val epcSupersededFactory: ObjectFactory<EpcSupersededStepConfig>,
    val checkAutomatchedEpcFactory: ObjectFactory<CheckEpcStepConfig>,
    val checkSearchedEpcFactory: ObjectFactory<CheckEpcStepConfig>,
    private val fooCheckYourAnswersStepFactory: ObjectFactory<FooCheckAnswersStepConfig>,
    private val journeyStateServiceFactory2: ObjectFactory<JourneyStateServiceFactory>,
) {
    final fun createJourneySteps(journeyId: String): Map<String, StepLifecycleOrchestrator> =
        journey(createDynamicState(journeyId)) {
            unreachableStepRedirect { "task-list" }
            step("task-list", journey.taskListStep) {
                redirectToUrl { "task-list" }
            }
            step("occupied", journey.occupied) {
                parents { journey.taskListStep.always() }
                redirectToStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.households
                        YesOrNo.NO -> journey.fooCheckYourAnswersStep
                    }
                }
            }
            step("households", journey.households) {
                parents { journey.occupied.hasOutcome(YesOrNo.YES) }
                redirectToStep { journey.tenants }
            }
            step("tenants", journey.tenants) {
                parents { journey.households.hasOutcome(Complete.COMPLETE) }
                redirectToStep { journey.fooCheckYourAnswersStep }
            }
            step("has-epc", journey.epcQuestion) {
                parents { journey.taskListStep.always() }
                redirectToStep { mode ->
                    when (mode) {
                        EpcStatus.AUTOMATCHED -> journey.checkAutomatchedEpc
                        EpcStatus.NOT_AUTOMATCHED -> journey.searchForEpc
                        EpcStatus.NO_EPC -> journey.fooCheckYourAnswersStep
                    }
                }
            }
            step<YesOrNo, CheckEpcStepConfig>("check-automatched-epc", journey.checkAutomatchedEpc) {
                parents { journey.epcQuestion.hasOutcome(EpcStatus.AUTOMATCHED) }
                redirectToStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.fooCheckYourAnswersStep
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
                redirectToStep { mode ->
                    when (mode) {
                        EpcSearchResult.FOUND -> journey.checkSearchedEpc
                        EpcSearchResult.SUPERSEDED -> journey.epcSuperseded
                        EpcSearchResult.NOT_FOUND -> journey.epcNotFound
                    }
                }
            }
            step("superseded-epc", journey.epcSuperseded) {
                parents { journey.searchForEpc.hasOutcome(EpcSearchResult.SUPERSEDED) }
                redirectToStep { journey.checkSearchedEpc }
            }
            step<YesOrNo, CheckEpcStepConfig>("check-found-epc", journey.checkSearchedEpc) {
                parents {
                    OrParents(
                        journey.searchForEpc.hasOutcome(EpcSearchResult.FOUND),
                        journey.epcSuperseded.hasOutcome(Complete.COMPLETE),
                    )
                }
                redirectToStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.fooCheckYourAnswersStep
                        YesOrNo.NO -> journey.searchForEpc
                    }
                }
                stepSpecificInitialisation {
                    usingEpc { searchedEpc }
                }
            }
            step("epc-not-found", journey.epcNotFound) {
                parents { journey.searchForEpc.hasOutcome(EpcSearchResult.NOT_FOUND) }
                redirectToStep { journey.fooCheckYourAnswersStep }
            }
            step("check-your-answers", journey.fooCheckYourAnswersStep) {
                parents {
                    AndParents(
                        OrParents(
                            journey.occupied.hasOutcome(YesOrNo.NO),
                            journey.tenants.hasOutcome(Complete.COMPLETE),
                        ),
                        OrParents(
                            journey.epcQuestion.hasOutcome(EpcStatus.NO_EPC),
                            journey.checkAutomatchedEpc.hasOutcome(YesOrNo.YES),
                            journey.checkSearchedEpc.hasOutcome(YesOrNo.YES),
                            journey.epcNotFound.hasOutcome(Complete.COMPLETE),
                        ),
                    )
                }
                redirectToUrl { "/" }
            }
        }

    // TODO PRSD-1546: Reduce boilerplate by relying on dependency injection to extract journey id from request
    private fun createDynamicState(journeyId: String) =
        FooJourney(
            JourneyStep(taskListStepFactory.getObject()),
            JourneyStep(occupiedFactory.getObject()),
            JourneyStep(householdsFactory.getObject()),
            JourneyStep(tenantsFactory.getObject()),
            JourneyStep(epcQuestionFactory.getObject()),
            JourneyStep(checkAutomatchedEpcFactory.getObject()),
            JourneyStep(searchForEpcFactory.getObject()),
            JourneyStep(epcNotFoundFactory.getObject()),
            JourneyStep(epcSupersededFactory.getObject()),
            JourneyStep(checkSearchedEpcFactory.getObject()),
            JourneyStep(fooCheckYourAnswersStepFactory.getObject()),
            journeyStateServiceFactory2.getObject().createForExistingJourney(),
        )

    final fun initializeJourneyState(propertyId: Long): String {
        val journeyId = propertyId.hashCode().toString()
        journeyStateServiceFactory2
            .getObject()
            .createForNewJourney()
            .setValue("propertyId", Json.encodeToString(serializer(), propertyId))
        return journeyId
    }
}

class FooJourney(
    val taskListStep: JourneyStep<Complete, NoInputFormModel, FooJourney>,
    override val occupied: JourneyStep<YesOrNo, OccupancyFormModel, FooJourney>,
    override val households: JourneyStep<Complete, NumberOfHouseholdsFormModel, FooJourney>,
    override val tenants: JourneyStep<Complete, NumberOfPeopleFormModel, FooJourney>,
    override val epcQuestion: JourneyStep<EpcStatus, EpcFormModel, FooJourney>,
    override val checkAutomatchedEpc: JourneyStep<YesOrNo, CheckMatchedEpcFormModel, FooJourney>,
    override val searchForEpc: JourneyStep<EpcSearchResult, EpcLookupFormModel, FooJourney>,
    override val epcNotFound: JourneyStep<Complete, NoInputFormModel, FooJourney>,
    override val epcSuperseded: JourneyStep<Complete, NoInputFormModel, FooJourney>,
    override val checkSearchedEpc: JourneyStep<YesOrNo, CheckMatchedEpcFormModel, FooJourney>,
    val fooCheckYourAnswersStep: JourneyStep<Complete, NoInputFormModel, FooJourney>,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    OccupiedJourneyState,
    EpcJourneyState {
    override var automatchedEpc: EpcDataModel? by mutableDelegate("automatchedEpc", serializer())
    override var searchedEpc: EpcDataModel? by mutableDelegate("searchedEpc", serializer())
    override val propertyId: Long by requiredDelegate("propertyId", serializer())
}
