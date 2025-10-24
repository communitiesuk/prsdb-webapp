package uk.gov.communities.prsdb.webapp.journeys.example

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.beans.factory.ObjectFactory
import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.always
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.example.steps.CheckEpcStepConfig
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcSearchResult
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcStatus
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.FooCheckAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.FooTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel

@PrsdbWebService
class FooExampleJourneyFactory(
    private val stateFactory: ObjectFactory<FooJourneyState>,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()
        state.validateStateMatchesPropertyId(propertyId)

        return journey(stateFactory.getObject()) {
            unreachableStepStep { journey.taskListStep }
            step("task-list", journey.taskListStep) {
                nextUrl { "task-list" }
            }
            step("occupied", journey.occupied) {
                parents { journey.taskListStep.always() }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.households
                        YesOrNo.NO -> journey.fooCheckYourAnswersStep
                    }
                }
            }
            step("households", journey.households) {
                parents { journey.occupied.hasOutcome(YesOrNo.YES) }
                nextStep { journey.tenants }
            }
            step("tenants", journey.tenants) {
                parents { journey.households.hasOutcome(Complete.COMPLETE) }
                nextStep { journey.fooCheckYourAnswersStep }
            }
            step("has-epc", journey.epcQuestion) {
                parents { journey.taskListStep.always() }
                nextStep { mode ->
                    when (mode) {
                        EpcStatus.AUTOMATCHED -> journey.checkAutomatchedEpc
                        EpcStatus.NOT_AUTOMATCHED -> journey.searchForEpc
                        EpcStatus.NO_EPC -> journey.fooCheckYourAnswersStep
                    }
                }
            }
            step<YesOrNo, CheckEpcStepConfig>("check-automatched-epc", journey.checkAutomatchedEpc) {
                parents { journey.epcQuestion.hasOutcome(EpcStatus.AUTOMATCHED) }
                nextStep { mode ->
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
                nextStep { journey.fooCheckYourAnswersStep }
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
                nextUrl { "/" }
            }
        }
    }

    fun initializeJourneyState(propertyId: Long): String = stateFactory.getObject().initializeJourneyState(propertyId)
}

@PrsdbWebComponent
@Scope("prototype")
class FooJourneyState(
    val taskListStep: FooTaskListStep,
    override val occupied: JourneyStep<YesOrNo, OccupancyFormModel, FooJourneyState>,
    override val households: JourneyStep<Complete, NumberOfHouseholdsFormModel, FooJourneyState>,
    override val tenants: JourneyStep<Complete, NumberOfPeopleFormModel, FooJourneyState>,
    override val epcQuestion: JourneyStep<EpcStatus, EpcFormModel, FooJourneyState>,
    override val checkAutomatchedEpc: JourneyStep<YesOrNo, CheckMatchedEpcFormModel, FooJourneyState>,
    override val searchForEpc: JourneyStep<EpcSearchResult, EpcLookupFormModel, FooJourneyState>,
    override val epcNotFound: EpcNotFoundStep,
    override val epcSuperseded: EpcSupersededStep,
    override val checkSearchedEpc: JourneyStep<YesOrNo, CheckMatchedEpcFormModel, FooJourneyState>,
    val fooCheckYourAnswersStep: FooCheckAnswersStep,
    private val journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    OccupiedJourneyState,
    EpcJourneyState {
    override var automatchedEpc: EpcDataModel? by mutableDelegate("automatchedEpc", serializer())
    override var searchedEpc: EpcDataModel? by mutableDelegate("searchedEpc", serializer())
    override val propertyId: Long by requiredDelegate("propertyId", serializer())

    // TODO PRSD-1546: Choose where to initialize and validate journey state
    final fun initializeJourneyState(propertyId: Long): String {
        val journeyId = generateJourneyId(propertyId)

        journeyStateService
            .initialiseJourneyWithId(journeyId) {
                setValue("propertyId", Json.encodeToString(serializer(), propertyId))
            }
        return journeyId
    }

    final fun validateStateMatchesPropertyId(currentPropertyId: Long) {
        if (currentPropertyId != propertyId) {
            throw NoSuchJourneyException()
        }
    }

    companion object {
        fun generateJourneyId(propertyId: Long): String =
            "Foo Example Journey for property $propertyId"
                .hashCode()
                .toUInt()
                .times(111113111U)
                .and(0x7FFFFFFFu)
                .toString(36)
    }
}
