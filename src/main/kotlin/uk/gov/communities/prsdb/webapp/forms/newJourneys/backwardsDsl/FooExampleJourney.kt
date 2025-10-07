package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.AndParents
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.CheckEpcStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.Complete
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.EpcSearchResult
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.EpcStatus
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.FooCheckAnswersStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.FooTaskListStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.OrParents
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.SearchEpcStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.VisitableStep
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.applyConditionToParent
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.hasOutcome
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.FooJourneyState
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.InnerEpcJourney
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.JourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

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
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
) : FooJourneyState {
    final fun initialiseJourney(propertyId: Long): Map<String, VisitableStep> {
        if (initialised) {
            throw IllegalStateException("Journey already initialised")
        } else {
            innerStateService = InnerEpcJourney(journeyDataServiceFactory.create("BackwardsDsl-$propertyId"), propertyId)
            initialised = true
        }

        return mapOf(
            taskListStep.step("task-list") {
                parents { fooCheckYourAnswersStep.applyConditionToParent { true } }
                reachableWhen { true }
                state { this@FooExampleJourney }
                redirectTo { null }
            },
            occupied.step("occupied") {
                reachableWhen { true }
                state { this@FooExampleJourney }
                redirectTo {
                    when (it) {
                        YesOrNo.YES -> households
                        YesOrNo.NO -> fooCheckYourAnswersStep
                    }
                }
            },
            households.step("households") {
                parents { occupied.hasOutcome(YesOrNo.YES) }
                state { this@FooExampleJourney }
                redirectTo { tenants }
            },
            tenants.step("tenants") {
                parents { households.hasOutcome(Complete.COMPLETE) }
                state { this@FooExampleJourney }
                redirectTo { fooCheckYourAnswersStep }
            },
            epcQuestion.step("has-epc") {
                reachableWhen { true }
                state { this@FooExampleJourney }
                redirectTo {
                    when (it) {
                        EpcStatus.AUTOMATCHED -> checkAutomatchedEpc
                        EpcStatus.NOT_AUTOMATCHED -> searchForEpc
                        EpcStatus.NO_EPC -> fooCheckYourAnswersStep
                    }
                }
            },
            checkAutomatchedEpc
                .usingEpc { automatchedEpc }
                .step("check-automatched-epc") {
                    parents { epcQuestion.hasOutcome(EpcStatus.AUTOMATCHED) }
                    state { this@FooExampleJourney }
                    redirectTo {
                        when (it) {
                            YesOrNo.YES -> fooCheckYourAnswersStep
                            YesOrNo.NO -> searchForEpc
                        }
                    }
                },
            searchForEpc.step("search-for-epc") {
                parents { OrParents(epcQuestion.hasOutcome(EpcStatus.NOT_AUTOMATCHED), checkAutomatchedEpc.hasOutcome(YesOrNo.NO)) }
                state { this@FooExampleJourney }
                redirectTo {
                    when (it) {
                        EpcSearchResult.FOUND -> checkSearchedEpc
                        EpcSearchResult.SUPERSEDED -> epcSuperseded
                        EpcSearchResult.NOT_FOUND -> epcNotFound
                    }
                }
            },
            epcSuperseded.step("superseded-epc") {
                parents { searchForEpc.hasOutcome(EpcSearchResult.SUPERSEDED) }
                state { this@FooExampleJourney }
                redirectTo { checkSearchedEpc }
            },
            checkSearchedEpc
                .usingEpc { searchedEpc }
                .step("check-found-epc") {
                    parents {
                        OrParents(
                            searchForEpc.hasOutcome(EpcSearchResult.FOUND),
                            epcSuperseded.hasOutcome(Complete.COMPLETE),
                        )
                    }
                    state { this@FooExampleJourney }
                    redirectTo {
                        when (it) {
                            YesOrNo.YES -> fooCheckYourAnswersStep
                            YesOrNo.NO -> searchForEpc
                        }
                    }
                },
            epcNotFound.step("epc-not-found") {
                state { this@FooExampleJourney }
                redirectTo { fooCheckYourAnswersStep }
                parents { searchForEpc.hasOutcome(EpcSearchResult.NOT_FOUND) }
            },
            fooCheckYourAnswersStep.step("check-your-answers") {
                redirectTo { null }
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
            },
        )
    }

    final var initialised = false
        private set

    final lateinit var innerStateService: InnerEpcJourney
        private set

    override val journeyData: JourneyData
        get() = innerStateService.journeyData

    override fun addStepData(
        key: String,
        value: Any,
    ): JourneyState = innerStateService.addStepData(key, value)

    override var automatchedEpc: EpcDataModel?
        get() = innerStateService.automatchedEpc
        set(value) {
            innerStateService.automatchedEpc = value
        }

    override var searchedEpc: EpcDataModel?
        get() = innerStateService.searchedEpc
        set(value) {
            innerStateService.searchedEpc = value
        }

    override val propertyId: Long
        get() = innerStateService.propertyId
}
