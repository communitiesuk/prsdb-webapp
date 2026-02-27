package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.always
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcNotAutomatchedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.FireSafetyDeclarationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.KeepPropertySafeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.PropertyComplianceCheckableElements
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.PropertyComplianceCyaStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.PropertyComplianceTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.ResponsibilityToTenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.SearchForEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks.EicrTask
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks.GasSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import java.security.Principal

@PrsdbWebService
class NewPropertyComplianceJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyComplianceJourney>,
) {
    fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.isStateInitialized = true
        }

        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state, propertyId)
        } else {
            checkYourAnswersJourneyMap(state, checkingAnswersFor)
        }
    }

    private fun checkYourAnswersJourneyMap(
        state: PropertyComplianceJourney,
        checkingAnswersFor: PropertyComplianceCheckableElements,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepDestination { journey.returnToCyaPageDestination }
            configure {
                withAdditionalContentProperty { "title" to "propertyCompliance.title" }
            }
            configureFirst {
                backDestination { journey.returnToCyaPageDestination }
            }
            when (checkingAnswersFor) {
                PropertyComplianceCheckableElements.GAS_SAFETY -> checkAnswerTask(journey.gasSafetyTask)
                PropertyComplianceCheckableElements.EICR -> checkAnswerTask(journey.eicrTask)
                PropertyComplianceCheckableElements.EPC -> checkAnswerTask(journey.epcTask)
                PropertyComplianceCheckableElements.DECLARATION -> {
                    step(journey.fireSafetyStep) {
                        initialStep()
                        nextStep { journey.keepPropertySafeStep }
                        routeSegment(FireSafetyDeclarationStep.ROUTE_SEGMENT)
                    }
                    step(journey.keepPropertySafeStep) {
                        parents { journey.fireSafetyStep.isComplete() }
                        nextStep { journey.responsibilityToTenantsStep }
                        routeSegment(KeepPropertySafeStep.ROUTE_SEGMENT)
                    }
                    step(journey.responsibilityToTenantsStep) {
                        parents { journey.keepPropertySafeStep.isComplete() }
                        nextStep { journey.finishCyaStep }
                        routeSegment(ResponsibilityToTenantsStep.ROUTE_SEGMENT)
                    }
                }
            }
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
        }

    private fun mainJourneyMap(
        state: PropertyComplianceJourney,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepStep { journey.taskListStep }
            configure {
                withAdditionalContentProperty { "title" to "propertyCompliance.title" }
            }
            step(journey.taskListStep) {
                routeSegment(TASK_LIST_PATH_SEGMENT)
                initialStep()
                noNextDestination()
            }
            section {
                withHeadingMessageKey("propertyCompliance.taskList.upload.heading")
                task(journey.gasSafetyTask) {
                    parents { journey.taskListStep.always() }
                    nextStep { journey.eicrTask.firstStep }
                    saveProgress()
                }
                task(journey.eicrTask) {
                    parents {
                        journey.taskListStep.always()
                    }
                    nextStep { journey.epcTask.firstStep }
                    saveProgress()
                }
                task(journey.epcTask) {
                    parents { journey.taskListStep.always() }
                    nextStep { journey.fireSafetyStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("propertyCompliance.taskList.landlordResponsibilities.heading")
                step(journey.fireSafetyStep) {
                    routeSegment(FireSafetyDeclarationStep.ROUTE_SEGMENT)
                    parents { journey.taskListStep.always() }
                    nextStep { journey.keepPropertySafeStep }
                    saveProgress()
                }
                step(journey.keepPropertySafeStep) {
                    routeSegment(KeepPropertySafeStep.ROUTE_SEGMENT)
                    parents { journey.taskListStep.always() }
                    nextStep { journey.responsibilityToTenantsStep }
                    saveProgress()
                }
                step(journey.responsibilityToTenantsStep) {
                    routeSegment(ResponsibilityToTenantsStep.ROUTE_SEGMENT)
                    parents { journey.taskListStep.always() }
                    nextStep { journey.cyaStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("propertyCompliance.taskList.checkAndSubmit.heading")
                step(journey.cyaStep) {
                    routeSegment(AbstractCheckYourAnswersStep.ROUTE_SEGMENT)
                    parents {
                        AndParents(
                            journey.gasSafetyTask.isComplete(),
                            journey.eicrTask.isComplete(),
                            journey.epcTask.isComplete(),
                            journey.fireSafetyStep.isComplete(),
                            journey.keepPropertySafeStep.isComplete(),
                            journey.responsibilityToTenantsStep.isComplete(),
                        )
                    }
                    nextUrl { PropertyComplianceController.getPropertyComplianceConfirmationPath(propertyId) }
                }
            }
        }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeState(user)
}

@JourneyFrameworkComponent
class PropertyComplianceJourney(
    override val taskListStep: PropertyComplianceTaskListStep,
    // Gas safety task
    override val gasSafetyTask: GasSafetyTask,
    override val gasSafetyStep: GasSafetyStep,
    override val gasSafetyIssueDateStep: GasSafetyIssueDateStep,
    override val gasSafetyEngineerNumberStep: GasSafetyEngineerNumberStep,
    override val gasSafetyCertificateUploadStep: GasSafetyCertificateUploadStep,
    override val gasSafetyUploadConfirmationStep: GasSafetyUploadConfirmationStep,
    override val gasSafetyOutdatedStep: GasSafetyOutdatedStep,
    override val gasSafetyExemptionStep: GasSafetyExemptionStep,
    override val gasSafetyExemptionReasonStep: GasSafetyExemptionReasonStep,
    override val gasSafetyExemptionOtherReasonStep: GasSafetyExemptionOtherReasonStep,
    override val gasSafetyExemptionConfirmationStep: GasSafetyExemptionConfirmationStep,
    override val gasSafetyExemptionMissingStep: GasSafetyExemptionMissingStep,
    // EICR task
    override val eicrTask: EicrTask,
    override val eicrStep: EicrStep,
    override val eicrIssueDateStep: EicrIssueDateStep,
    override val eicrUploadStep: EicrUploadStep,
    override val eicrUploadConfirmationStep: EicrUploadConfirmationStep,
    override val eicrOutdatedStep: EicrOutdatedStep,
    override val eicrExemptionStep: EicrExemptionStep,
    override val eicrExemptionReasonStep: EicrExemptionReasonStep,
    override val eicrExemptionOtherReasonStep: EicrExemptionOtherReasonStep,
    override val eicrExemptionConfirmationStep: EicrExemptionConfirmationStep,
    override val eicrExemptionMissingStep: EicrExemptionMissingStep,
    // EPC task
    override val epcTask: EpcTask,
    override val epcQuestionStep: EpcQuestionStep,
    override val checkAutomatchedEpcStep: CheckMatchedEpcStep,
    override val epcNotAutomatchedStep: EpcNotAutomatchedStep,
    override val searchForEpcStep: SearchForEpcStep,
    override val epcSupersededStep: EpcSupersededStep,
    override val checkMatchedEpcStep: CheckMatchedEpcStep,
    override val epcNotFoundStep: EpcNotFoundStep,
    override val epcMissingStep: EpcMissingStep,
    override val epcExemptionReasonStep: EpcExemptionReasonStep,
    override val epcExemptionConfirmationStep: EpcExemptionConfirmationStep,
    override val meesExemptionCheckStep: MeesExemptionCheckStep,
    override val meesExemptionReasonStep: MeesExemptionReasonStep,
    override val meesExemptionConfirmationStep: MeesExemptionConfirmationStep,
    override val lowEnergyRatingStep: LowEnergyRatingStep,
    override val epcExpiryCheckStep: EpcExpiryCheckStep,
    override val epcExpiredStep: EpcExpiredStep,
    // Landlord Responsibilties
    override val fireSafetyStep: FireSafetyDeclarationStep,
    override val keepPropertySafeStep: KeepPropertySafeStep,
    override val responsibilityToTenantsStep: ResponsibilityToTenantsStep,
    // CYA
    override val cyaStep: PropertyComplianceCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep<PropertyComplianceCheckableElements>,
    private val journeyStateService: JourneyStateService,
    private val objectFactory: ObjectFactory<PropertyComplianceJourneyState>,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    PropertyComplianceJourneyState {
    override var automatchedEpc: EpcDataModel? by delegateProvider.nullableDelegate("automatchedEpc")
    override var searchedEpc: EpcDataModel? by delegateProvider.nullableDelegate("searchedEpc")
    override var acceptedEpc: EpcDataModel? by delegateProvider.nullableDelegate("acceptedEpc")
    override var propertyId: Long by delegateProvider.requiredDelegate("propertyId")
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var cyaJourneys: Map<PropertyComplianceCheckableElements, String> by delegateProvider.requiredDelegate(
        "checkYourAnswersChildJourneyId",
        mapOf(),
    )
    override var checkingAnswersFor: PropertyComplianceCheckableElements? by delegateProvider.nullableDelegate("checkingAnswersFor")

    private var cyaRouteSegment: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override var returnToCyaPageDestination: Destination
        get() = cyaRouteSegment?.let { Destination.StepRoute(it, baseJourneyId) } ?: Destination.Nowhere()
        set(destination) {
            cyaRouteSegment =
                when (destination) {
                    is Destination.StepRoute -> destination.routeSegment
                    is Destination.VisitableStep -> destination.step.routeSegment
                    else -> null
                }
        }

    override fun createChildJourneyState(cyaJourneyId: String): PropertyComplianceJourneyState {
        copyJourneyTo(cyaJourneyId)
        return objectFactory.getObject().apply { setJourneyId(cyaJourneyId) }
    }
}

interface PropertyComplianceJourneyState :
    JourneyState,
    GasSafetyState,
    EicrState,
    EpcState,
    CheckYourAnswersJourneyState<PropertyComplianceCheckableElements> {
    val taskListStep: PropertyComplianceTaskListStep
    val gasSafetyTask: GasSafetyTask
    val eicrTask: EicrTask
    val epcTask: EpcTask
    val fireSafetyStep: FireSafetyDeclarationStep
    val keepPropertySafeStep: KeepPropertySafeStep
    val responsibilityToTenantsStep: ResponsibilityToTenantsStep
    override val finishCyaStep: FinishCyaJourneyStep<PropertyComplianceCheckableElements>
    override val cyaStep: PropertyComplianceCyaStep
}
