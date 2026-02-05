package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AndParents
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
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.FireSafetyDeclarationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.KeepPropertySafeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.PropertyComplianceCyaStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.PropertyComplianceTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.ResponsibilityToTenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks.EicrTask
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks.GasSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkable
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

        return journey(state) {
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
                    checkable()
                    saveProgress()
                }
                task(journey.eicrTask) {
                    parents { journey.taskListStep.always() }
                    nextStep { journey.epcTask.firstStep }
                    checkable()
                    saveProgress()
                }
                task(journey.epcTask) {
                    parents { journey.taskListStep.always() }
                    nextStep { journey.fireSafetyStep }
                    checkable()
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("propertyCompliance.taskList.landlordResponsibilities.heading")
                step(journey.fireSafetyStep) {
                    routeSegment(FireSafetyDeclarationStep.ROUTE_SEGMENT)
                    parents { journey.taskListStep.always() }
                    nextStep { journey.keepPropertySafeStep }
                    checkable()
                    saveProgress()
                }
                step(journey.keepPropertySafeStep) {
                    routeSegment(KeepPropertySafeStep.ROUTE_SEGMENT)
                    parents { journey.taskListStep.always() }
                    nextStep { journey.responsibilityToTenantsStep }
                    checkable()
                    saveProgress()
                }
                step(journey.responsibilityToTenantsStep) {
                    routeSegment(ResponsibilityToTenantsStep.ROUTE_SEGMENT)
                    parents { journey.taskListStep.always() }
                    nextStep { journey.cyaStep }
                    checkable()
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
                        )
                    }
                    // TODO PDJB-467 - do we need the full route for this? Do we even have it if we need the propertyOwernshipId?
                    nextUrl { CONFIRMATION_PATH_SEGMENT }
                }
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
    override val gasSafetyEngineerNumberStep: GasSafetyEngineerNumberStep,
    override val gasSafetyCertificateUploadStep: GasSafetyCertificateUploadStep,
    override val gasSafetyUploadConfirmationStep: GasSafetyUploadConfirmationStep,
    // EICR task
    override val eicrTask: EicrTask,
    // EPC task
    override val epcTask: EpcTask,
    override val epcQuestionStep: EpcQuestionStep,
    // Landlord Responsibilties
    override val fireSafetyStep: FireSafetyDeclarationStep,
    override val keepPropertySafeStep: KeepPropertySafeStep,
    override val responsibilityToTenantsStep: ResponsibilityToTenantsStep,
    // CYA
    override val cyaStep: PropertyComplianceCyaStep,
    private val journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    PropertyComplianceJourneyState {
    override var automatchedEpc: EpcDataModel? by delegateProvider.nullableDelegate("automatchedEpc")
    override var searchedEpc: EpcDataModel? by delegateProvider.nullableDelegate("searchedEpc")
    override var propertyId: Long by delegateProvider.requiredDelegate("propertyId")
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var cyaChildJourneyIdIfInitialized: String? by delegateProvider.nullableDelegate("checkYourAnswersChildJourneyId")
}

interface PropertyComplianceJourneyState :
    JourneyState,
    GasSafetyState,
    EicrState,
    EpcState,
    CheckYourAnswersJourneyState {
    val taskListStep: PropertyComplianceTaskListStep
    val gasSafetyTask: GasSafetyTask
    val eicrTask: EicrTask
    val epcTask: EpcTask
    val fireSafetyStep: FireSafetyDeclarationStep
    val keepPropertySafeStep: KeepPropertySafeStep
    val responsibilityToTenantsStep: ResponsibilityToTenantsStep
    override val cyaStep: PropertyComplianceCyaStep
}
