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
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.VisitableStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.applyConditionToParent
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.hasOutcome
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.SimpleJourneyState
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@PrsdbWebService
class BackwardsDslJourneyDelegate(
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
        val occupiedStep: OccupiedStep = occupiedStepFactory.getObject()
        val householdsStep: HouseholdStep = householdsStepFactory.getObject()
        val tenantsStep: TenantsStep = tenantsStepFactory.getObject()
        val epcQuestionStep = epcQuestionStepFactory.getObject()
        val epcSearchEpcStep = epcSearchEpcStepFactory.getObject()
        val epcNotFoundStep = epcNotFoundStepFactory.getObject()
        val epcSupersededStep = epcSupersededStepFactory.getObject()
        val checkAutomatchedEpcStep = epcStepFactory.getObject()
        val checkSearchEpcStep = epcStepFactory.getObject()
        val fooCheckYourAnswersStep = fooCheckYourAnswersStepFactory.getObject()

        val journeyDataService = journeyDataServiceFactory.create("BackwardsDsl-$propertyId")
        return mapOf(
            taskListStep.step("task-list") {
                parents { fooCheckYourAnswersStep.applyConditionToParent { true } }
                reachableWhen { true }
                state { SimpleJourneyState(journeyDataService, propertyId) }
                redirectTo { null }
            },
            occupiedStep.step("occupied") {
                reachableWhen { true }
                state { SimpleJourneyState(journeyDataService, propertyId) }
                redirectTo {
                    when (it) {
                        YesOrNo.YES -> householdsStep
                        YesOrNo.NO -> fooCheckYourAnswersStep
                    }
                }
            },
            householdsStep.step("households") {
                parents { occupiedStep.hasOutcome(YesOrNo.YES) }
                state { SimpleJourneyState(journeyDataService, propertyId) }
                redirectTo { tenantsStep }
            },
            tenantsStep.step("tenants") {
                parents { householdsStep.hasOutcome(Complete.COMPLETE) }
                state { SimpleJourneyState(journeyDataService, propertyId) }
                redirectTo { fooCheckYourAnswersStep }
            },
            epcQuestionStep.step("has-epc") {
                reachableWhen { true }
                state { SimpleJourneyState(journeyDataService, propertyId) }
                redirectTo {
                    when (it) {
                        EpcStatus.AUTOMATCHED -> checkAutomatchedEpcStep
                        EpcStatus.NOT_AUTOMATCHED -> epcSearchEpcStep
                        EpcStatus.NO_EPC -> fooCheckYourAnswersStep
                    }
                }
            },
            checkAutomatchedEpcStep.step("check-automatched-epc") {
                parents { epcQuestionStep.hasOutcome(EpcStatus.AUTOMATCHED) }
                state { SimpleJourneyState(journeyDataService, propertyId) }
                redirectTo {
                    when (it) {
                        YesOrNo.YES -> fooCheckYourAnswersStep
                        YesOrNo.NO -> epcSearchEpcStep
                    }
                }
            },
            epcSearchEpcStep.step("search-for-epc") {
                parents { OrParents(epcQuestionStep.hasOutcome(EpcStatus.NOT_AUTOMATCHED), checkAutomatchedEpcStep.hasOutcome(YesOrNo.NO)) }
                state { SimpleJourneyState(journeyDataService, propertyId) }
                redirectTo {
                    when (it) {
                        EpcSearchResult.FOUND -> checkSearchEpcStep
                        EpcSearchResult.SUPERSEDED -> epcSupersededStep
                        EpcSearchResult.NOT_FOUND -> epcNotFoundStep
                    }
                }
            },
            epcSupersededStep.step("superseded-epc") {
                parents { epcSearchEpcStep.hasOutcome(EpcSearchResult.SUPERSEDED) }
                reachableWhen { epcSearchEpcStep.outcome() == EpcSearchResult.SUPERSEDED }
                state { SimpleJourneyState(journeyDataService, propertyId) }
                redirectTo { checkSearchEpcStep }
            },
            checkSearchEpcStep.step("check-found-epc") {
                parents { OrParents(epcSearchEpcStep.hasOutcome(EpcSearchResult.FOUND), epcSupersededStep.hasOutcome(Complete.COMPLETE)) }
                state { SimpleJourneyState(journeyDataService, propertyId) }
                redirectTo {
                    when (it) {
                        YesOrNo.YES -> fooCheckYourAnswersStep
                        YesOrNo.NO -> epcSearchEpcStep
                    }
                }
            },
            epcNotFoundStep.step("epc-not-found") {
                state { SimpleJourneyState(journeyDataService, propertyId) }
                redirectTo { fooCheckYourAnswersStep }
                parents { epcSearchEpcStep.hasOutcome(EpcSearchResult.NOT_FOUND) }
            },
            fooCheckYourAnswersStep.step("check-your-answers") {
                state { SimpleJourneyState(journeyDataService, propertyId) }
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
                            checkSearchEpcStep.hasOutcome(YesOrNo.YES),
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
