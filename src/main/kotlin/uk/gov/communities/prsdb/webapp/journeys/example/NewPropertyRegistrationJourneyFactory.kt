package uk.gov.communities.prsdb.webapp.journeys.example

import kotlinx.serialization.serializer
import org.springframework.beans.factory.ObjectFactory
import org.springframework.context.annotation.Scope
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.PROPERTY_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.always
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.AddressState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalCouncilStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.AddressTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OccupationTask
import uk.gov.communities.prsdb.webapp.journeys.shared.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.CheckYourAnswersJourneyState.Companion.checkYourAnswersJourney
import uk.gov.communities.prsdb.webapp.journeys.shared.CheckYourAnswersJourneyState.Companion.checkable
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import java.security.Principal

@PrsdbWebService
class NewPropertyRegistrationJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyRegistrationJourneyState>,
) {
    final fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.taskListStep }
            step(TASK_LIST_PATH_SEGMENT, journey.taskListStep) {
                initialStep()
                noNextDestination()
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.register.heading")
                task(journey.addressTask) {
                    parents { journey.taskListStep.always() }
                    nextStep { journey.propertyTypeStep }
                    checkable()
                }
                step("property-type", journey.propertyTypeStep) {
                    parents { journey.addressTask.isComplete() }
                    nextStep { journey.ownershipTypeStep }
                    checkable()
                }
                step("ownership-type", journey.ownershipTypeStep) {
                    parents { journey.propertyTypeStep.isComplete() }
                    nextStep { journey.licensingTask.firstStep }
                    checkable()
                }
                task(journey.licensingTask) {
                    parents { journey.ownershipTypeStep.isComplete() }
                    nextStep { journey.occupationTask.firstStep }
                    checkable()
                }
                task(journey.occupationTask) {
                    parents { journey.licensingTask.isComplete() }
                    nextStep { journey.cyaStep }
                    checkable()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.checkAndSubmit.heading")
                step("check-your-answers", journey.cyaStep) {
                    parents { journey.occupationTask.isComplete() }
                    nextUrl { "$PROPERTY_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
                }
            }
            checkYourAnswersJourney()
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeJourneyState(user)
}

@PrsdbWebComponent
@Scope("prototype")
class PropertyRegistrationJourneyState(
    val taskListStep: PropertyRegistrationTaskListStep,
    override val lookupStep: LookupAddressStep,
    override val selectAddressStep: SelectAddressStep,
    override val alreadyRegisteredStep: AlreadyRegisteredStep,
    override val noAddressFoundStep: NoAddressFoundStep,
    override val manualAddressStep: ManualAddressStep,
    override val localCouncilStep: LocalCouncilStep,
    val addressTask: AddressTask,
    val propertyTypeStep: PropertyTypeStep,
    val ownershipTypeStep: OwnershipTypeStep,
    override val licensingTypeStep: LicensingTypeStep,
    override val selectiveLicenceStep: SelectiveLicenceStep,
    override val hmoMandatoryLicenceStep: HmoMandatoryLicenceStep,
    override val hmoAdditionalLicenceStep: HmoAdditionalLicenceStep,
    val licensingTask: LicensingTask,
    override val occupied: OccupiedStep,
    override val households: HouseholdStep,
    override val tenants: TenantsStep,
    val occupationTask: OccupationTask,
    override val cyaStep: RequestableStep<Complete, CheckAnswersFormModel, PropertyRegistrationJourneyState>,
    private val journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    AddressState,
    LicensingState,
    OccupiedJourneyState,
    CheckYourAnswersJourneyState {
    override var cachedAddresses: List<AddressDataModel>? by mutableDelegate("cachedAddresses", serializer())
    override var isAddressAlreadyRegistered: Boolean? by mutableDelegate("isAddressAlreadyRegistered", serializer())

    final fun initializeJourneyState(user: Principal): String {
        val journeyId = generateJourneyId(user)

        journeyStateService
            .initialiseJourneyWithId(journeyId) {}
        return journeyId
    }

    fun getSubmittedStepData() = journeyStateService.getSubmittedStepData()

    override var cyaChildJourneyId: String? by mutableDelegate("cyaChildJourneyId", serializer())
        private set

    override val baseJourneyId: String
        get() = journeyStateService.journeyMetadata.baseJourneyId ?: journeyStateService.journeyId

    override val isCheckingAnswers: Boolean
        get() = journeyStateService.journeyMetadata.childJourneyName != null

    fun initialiseCyaChildJourney() {
        val newId = generateJourneyId(SecurityContextHolder.getContext().authentication)
        journeyStateService.initialiseChildJourney(newId, "checkYourAnswers")
        cyaChildJourneyId = newId
    }

    companion object {
        fun generateJourneyId(user: Principal): String =
            "Prop reg journey for user ${user.name} at time ${System.currentTimeMillis()}"
                .hashCode()
                .toUInt()
                .times(111113111U)
                .and(0x7FFFFFFFu)
                .toString(36)
    }
}
