package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl

import org.springframework.beans.factory.ObjectFactory
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.newJourneys.Complete
import uk.gov.communities.prsdb.webapp.forms.newJourneys.EpcSearchResult
import uk.gov.communities.prsdb.webapp.forms.newJourneys.EpcStatus
import uk.gov.communities.prsdb.webapp.forms.newJourneys.YesOrNo
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.AndParents
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.CheckEpcStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.FooCheckAnswersStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.FooTaskListStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.OrParents
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.SearchEpcStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.UsableStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.VisitableStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.applyConditionToParent
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.hasOutcome
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.FooJourneyState
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.PartialEpcJourney
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@PrsdbWebService
class FooExampleJourney(
    private val taskListStepFactory: ObjectFactory<FooTaskListStep>,
    private val occupiedStepFactory: ObjectFactory<OccupiedStep>,
    private val householdsStepFactory: ObjectFactory<HouseholdStep>,
    private val tenantsStepFactory: ObjectFactory<TenantsStep>,
    private val epcQuestionStepFactory: ObjectFactory<EpcQuestionStep>,
    private val epcSearchEpcStepFactory: ObjectFactory<SearchEpcStep>,
    private val epcNotFoundStepFactory: ObjectFactory<EpcNotFoundStep>,
    private val epcSupersededStepFactory: ObjectFactory<EpcSupersededStep>,
    private val epcStepFactory: ObjectFactory<CheckEpcStep>,
    private val fooCheckYourAnswersStepFactory: ObjectFactory<FooCheckAnswersStep>,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
) {
    fun initialiseJourney(propertyId: Long): Map<String, VisitableStep> {
        val taskListStep = taskListStepFactory.getObject()
        val occupiedStep = occupiedStepFactory.getObject()
        val householdsStep = householdsStepFactory.getObject()
        val tenantsStep = tenantsStepFactory.getObject()
        val epcQuestionStep = epcQuestionStepFactory.getObject()
        val searchForEpcStep = epcSearchEpcStepFactory.getObject()
        val epcNotFoundStep = epcNotFoundStepFactory.getObject()
        val epcSupersededStep = epcSupersededStepFactory.getObject()
        val checkAutomatchedEpcStep = epcStepFactory.getObject()
        val checkSearchedEpcStep = epcStepFactory.getObject()
        val fooCheckYourAnswersStep = fooCheckYourAnswersStepFactory.getObject()

        val journeyDataService = journeyDataServiceFactory.create("BackwardsDsl-$propertyId")

        val state =
            object : PartialEpcJourney(journeyDataService, propertyId), FooJourneyState {
                override val epcQuestion: UsableStep<EpcFormModel>
                    get() = epcQuestionStep
                override val checkAutomatchedEpc: UsableStep<CheckMatchedEpcFormModel>
                    get() = checkAutomatchedEpcStep
                override val searchForEpc: UsableStep<EpcLookupFormModel>
                    get() = searchForEpcStep
                override val epcNotFound: UsableStep<NoInputFormModel>
                    get() = epcNotFoundStep
                override val epcSuperseded: UsableStep<NoInputFormModel>
                    get() = epcSupersededStep
                override val checkSearchedEpc: UsableStep<CheckMatchedEpcFormModel>
                    get() = checkSearchedEpcStep

                override val occupied: UsableStep<OccupancyFormModel>
                    get() = occupiedStep
                override val households: UsableStep<NumberOfHouseholdsFormModel>
                    get() = householdsStep
                override val tenants: UsableStep<NumberOfPeopleFormModel>
                    get() = tenantsStep
            }

        return mapOf(
            taskListStep.step("task-list") {
                parents { fooCheckYourAnswersStep.applyConditionToParent { true } }
                reachableWhen { true }
                state { state }
                redirectTo { null }
            },
            occupiedStep.step("occupied") {
                reachableWhen { true }
                state { state }
                redirectTo {
                    when (it) {
                        YesOrNo.YES -> householdsStep
                        YesOrNo.NO -> fooCheckYourAnswersStep
                    }
                }
            },
            householdsStep.step("households") {
                parents { occupiedStep.hasOutcome(YesOrNo.YES) }
                state { state }
                redirectTo { tenantsStep }
            },
            tenantsStep.step("tenants") {
                parents { householdsStep.hasOutcome(Complete.COMPLETE) }
                state { state }
                redirectTo { fooCheckYourAnswersStep }
            },
            epcQuestionStep.step("has-epc") {
                reachableWhen { true }
                state { state }
                redirectTo {
                    when (it) {
                        EpcStatus.AUTOMATCHED -> checkAutomatchedEpcStep
                        EpcStatus.NOT_AUTOMATCHED -> searchForEpcStep
                        EpcStatus.NO_EPC -> fooCheckYourAnswersStep
                    }
                }
            },
            checkAutomatchedEpcStep
                .usingEpc { automatchedEpc }
                .step("check-automatched-epc") {
                    parents { epcQuestionStep.hasOutcome(EpcStatus.AUTOMATCHED) }
                    state { state }
                    redirectTo {
                        when (it) {
                            YesOrNo.YES -> fooCheckYourAnswersStep
                            YesOrNo.NO -> searchForEpcStep
                        }
                    }
                },
            searchForEpcStep.step("search-for-epc") {
                parents { OrParents(epcQuestionStep.hasOutcome(EpcStatus.NOT_AUTOMATCHED), checkAutomatchedEpcStep.hasOutcome(YesOrNo.NO)) }
                state { state }
                redirectTo {
                    when (it) {
                        EpcSearchResult.FOUND -> checkSearchedEpcStep
                        EpcSearchResult.SUPERSEDED -> epcSupersededStep
                        EpcSearchResult.NOT_FOUND -> epcNotFoundStep
                    }
                }
            },
            epcSupersededStep.step("superseded-epc") {
                parents { searchForEpcStep.hasOutcome(EpcSearchResult.SUPERSEDED) }
                reachableWhen { searchForEpcStep.outcome() == EpcSearchResult.SUPERSEDED }
                state { state }
                redirectTo { checkSearchedEpcStep }
            },
            checkSearchedEpcStep
                .usingEpc { searchedEpc }
                .step("check-found-epc") {
                    parents {
                        OrParents(
                            searchForEpcStep.hasOutcome(EpcSearchResult.FOUND),
                            epcSupersededStep.hasOutcome(Complete.COMPLETE),
                        )
                    }
                    state { state }
                    redirectTo {
                        when (it) {
                            YesOrNo.YES -> fooCheckYourAnswersStep
                            YesOrNo.NO -> searchForEpcStep
                        }
                    }
                },
            epcNotFoundStep.step("epc-not-found") {
                state { state }
                redirectTo { fooCheckYourAnswersStep }
                parents { searchForEpcStep.hasOutcome(EpcSearchResult.NOT_FOUND) }
            },
            fooCheckYourAnswersStep.step("check-your-answers") {
                state { state }
                redirectTo { null }
                parents {
                    AndParents(
                        OrParents(
                            occupiedStep.hasOutcome(YesOrNo.NO),
                            tenantsStep.hasOutcome(Complete.COMPLETE),
                        ),
                        OrParents(
                            epcQuestionStep.hasOutcome(EpcStatus.NO_EPC),
                            checkAutomatchedEpcStep.hasOutcome(YesOrNo.YES),
                            checkSearchedEpcStep.hasOutcome(YesOrNo.YES),
                            epcNotFoundStep.hasOutcome(Complete.COMPLETE),
                        ),
                    )
                }
            },
        )
    }

    fun getStepModelAndView(
        stepName: String,
        propertyId: Long,
    ): ModelAndView =
        initialiseJourney(propertyId)[stepName]?.getStepModelAndView()
            ?: throw IllegalArgumentException("Step $stepName not found")

    fun postStepModelAndView(
        stepName: String,
        formData: PageData,
        propertyId: Long,
    ): ModelAndView =
        initialiseJourney(propertyId)[stepName]?.postStepModelAndView(formData)
            ?: throw IllegalArgumentException("Step $stepName not found")
}
