package uk.gov.communities.prsdb.webapp.journeys.example

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
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
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@PrsdbWebService
class FooExampleJourneyFactory(
    private val taskListStepFactory: ObjectFactory<FooTaskListStep>,
    val occupiedFactory: ObjectFactory<OccupiedStep>,
    val householdsFactory: ObjectFactory<HouseholdStep>,
    val tenantsFactory: ObjectFactory<TenantsStep>,
    val epcQuestionFactory: ObjectFactory<EpcQuestionStep>,
    val searchForEpcFactory: ObjectFactory<SearchEpcStep>,
    val epcNotFoundFactory: ObjectFactory<EpcNotFoundStep>,
    val epcSupersededFactory: ObjectFactory<EpcSupersededStep>,
    val checkAutomatchedEpcFactory: ObjectFactory<CheckEpcStep>,
    val checkSearchedEpcFactory: ObjectFactory<CheckEpcStep>,
    private val fooCheckYourAnswersStepFactory: ObjectFactory<FooCheckAnswersStep>,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
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
            step<YesOrNo, CheckEpcStep>("check-automatched-epc", journey.checkAutomatchedEpc) {
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
            step<YesOrNo, CheckEpcStep>("check-found-epc", journey.checkSearchedEpc) {
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
            JourneyStateService(journeyDataServiceFactory.create(journeyId)),
        )

    final fun journeyStateInitialisation(
        journeyId: String,
        propertyId: Long,
    ) {
        JourneyStateService(
            journeyDataServiceFactory.create(journeyId),
        ).setValue("propertyId", Json.encodeToString(serializer(), propertyId))
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
    override var automatchedEpc: EpcDataModel? by delegate("automatchedEpc", serializer())
    override var searchedEpc: EpcDataModel? by delegate("searchedEpc", serializer())
    override val propertyId: Long by compulsoryDelegate("propertyId", serializer())
}
