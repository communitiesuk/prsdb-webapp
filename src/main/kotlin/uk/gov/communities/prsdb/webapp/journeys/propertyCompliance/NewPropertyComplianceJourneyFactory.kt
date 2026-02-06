package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.always
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.PropertyComplianceTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks.GasSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkable
import java.security.Principal

@PrsdbWebService
class NewPropertyComplianceJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyComplianceJourneyState>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()
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
                    // TODO PDJB-467 - set next step to EICR task
                    noNextDestination()
                    checkable()
                    saveProgress()
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
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    PropertyComplianceJourneyState

interface PropertyComplianceJourneyState :
    JourneyState,
    GasSafetyState {
    val taskListStep: PropertyComplianceTaskListStep
    val gasSafetyTask: GasSafetyTask
}
