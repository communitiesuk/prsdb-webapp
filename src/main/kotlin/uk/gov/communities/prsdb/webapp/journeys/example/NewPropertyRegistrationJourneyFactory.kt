package uk.gov.communities.prsdb.webapp.journeys.example

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.PROPERTY_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.IncompletePropertyCreatingNavigationalStep
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.always
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.AddressState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
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
            step(journey.taskListStep) {
                routeSegment(TASK_LIST_PATH_SEGMENT)
                initialStep()
                noNextDestination()
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.register.heading")
                task(journey.addressTask) {
                    parents { journey.taskListStep.always() }
                    nextStep { journey.propertyTypeStep }
                    checkable()
                    saveProgress()
                    customExitStep(journey.addressExitStep)
                }
                step(journey.propertyTypeStep) {
                    routeSegment("property-type")
                    parents { journey.addressTask.isComplete() }
                    nextStep { journey.ownershipTypeStep }
                    checkable()
                    saveProgress()
                }
                step(journey.ownershipTypeStep) {
                    routeSegment("ownership-type")
                    parents { journey.propertyTypeStep.isComplete() }
                    nextStep { journey.licensingTask.firstStep }
                    checkable()
                    saveProgress()
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
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.checkAndSubmit.heading")
                step(journey.cyaStep) {
                    routeSegment("check-answers")
                    parents { journey.occupationTask.isComplete() }
                    nextUrl { "$PROPERTY_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
                }
            }
            checkYourAnswersJourney()
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeState(user)
}

@JourneyFrameworkComponent
class PropertyRegistrationJourney(
    // Task list step
    override val taskListStep: PropertyRegistrationTaskListStep,
    // Address task
    override val addressTask: AddressTask,
    override val lookupStep: LookupAddressStep,
    override val selectAddressStep: SelectAddressStep,
    override val alreadyRegisteredStep: AlreadyRegisteredStep,
    override val noAddressFoundStep: NoAddressFoundStep,
    override val manualAddressStep: ManualAddressStep,
    override val localCouncilStep: LocalCouncilStep,
    override val addressExitStep: IncompletePropertyCreatingNavigationalStep,
    // Property details steps
    override val propertyTypeStep: PropertyTypeStep,
    override val ownershipTypeStep: OwnershipTypeStep,
    // Licensing task
    override val licensingTask: LicensingTask,
    override val licensingTypeStep: LicensingTypeStep,
    override val selectiveLicenceStep: SelectiveLicenceStep,
    override val hmoMandatoryLicenceStep: HmoMandatoryLicenceStep,
    override val hmoAdditionalLicenceStep: HmoAdditionalLicenceStep,
    // Occupation task
    override val occupationTask: OccupationTask,
    override val occupied: OccupiedStep,
    override val households: HouseholdStep,
    override val tenants: TenantsStep,
    override val bedrooms: BedroomsStep,
    override val rentIncludesBills: RentIncludesBillsStep,
    // Check your answers step
    override val cyaStep: RequestableStep<Complete, CheckAnswersFormModel, PropertyRegistrationJourneyState>,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    PropertyRegistrationJourneyState {
    override var cachedAddresses: List<AddressDataModel>? by delegateProvider.mutableDelegate("cachedAddresses")
    override var isAddressAlreadyRegistered: Boolean? by delegateProvider.mutableDelegate("isAddressAlreadyRegistered")
    override var cyaChildJourneyId: String? by delegateProvider.mutableDelegate("checkYourAnswersChildJourneyId")

    override fun generateJourneyId(seed: Any?): String {
        val user = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(user?.let { generateSeedForUser(it) })
    }

    companion object {
        fun generateSeedForUser(user: Principal): String = "Prop reg journey for user ${user.name} at time ${System.currentTimeMillis()}"
    }
}

interface PropertyRegistrationJourneyState :
    AddressState,
    LicensingState,
    OccupationState,
    CheckYourAnswersJourneyState {
    val taskListStep: PropertyRegistrationTaskListStep
    val addressTask: AddressTask
    val addressExitStep: IncompletePropertyCreatingNavigationalStep
    val propertyTypeStep: PropertyTypeStep
    val ownershipTypeStep: OwnershipTypeStep
    val licensingTask: LicensingTask
    val occupationTask: OccupationTask
    override val cyaStep: RequestableStep<Complete, CheckAnswersFormModel, PropertyRegistrationJourneyState>
}
