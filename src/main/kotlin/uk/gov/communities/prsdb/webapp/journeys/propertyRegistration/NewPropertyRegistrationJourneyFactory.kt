package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.PROPERTY_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.always
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyRegistrationAddressState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyJointLandlordsInvitedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalCouncilStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationCyaStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.JointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OccupationTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyRegistrationAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkYourAnswersJourney
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkable
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import java.security.Principal

@PrsdbWebService
class NewPropertyRegistrationJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyRegistrationJourneyState>,
) {
    final fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return when (state.checkingAnswersFor) {
            null -> mainJourneyMap(state)
            CheckableElements.PROPERTY_TYPE -> checkPropertyTypeMap(state)
            CheckableElements.OWNERSHIP_TYPE -> TODO()
            CheckableElements.LICENSING -> TODO()
            CheckableElements.OCCUPATION -> TODO()
            CheckableElements.HOUSEHOLDS -> TODO()
            CheckableElements.TENANTS -> TODO()
            CheckableElements.RENT_LEVELS -> TODO()
            CheckableElements.JOINT_LANDLORDS -> TODO()
        }
    }

    private fun checkPropertyTypeMap(state: PropertyRegistrationJourneyState): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            step(journey.propertyTypeStep) {
                backDestination {
                    Destination.ExternalUrl(
                        "check-answers",
                        mapOf("journeyId" to (journey.realBaseJourneyIdForCya ?: journey.journeyId)),
                    )
                }
                routeSegment("property-type")
                initialStep()
                nextStep { journey.finishCyaStep }
            }
            step(journey.finishCyaStep) {
                parents { journey.propertyTypeStep.isComplete() }
                nextDestination { Destination.Nowhere() }
            }
            unreachableStepDestination {
                Destination.ExternalUrl(
                    "check-answers",
                    mapOf("journeyId" to (journey.realBaseJourneyIdForCya ?: journey.journeyId)),
                )
            }
            configure {
                withAdditionalContentProperty { "title" to "registerProperty.title" }
            }
        }

    private fun mainJourneyMap(state: PropertyRegistrationJourneyState): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepStep { journey.taskListStep }
            configure {
                withAdditionalContentProperty { "title" to "registerProperty.title" }
            }
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
                    saveProgress()
                }
                task(journey.occupationTask) {
                    parents { journey.licensingTask.isComplete() }
                    nextStep { journey.jointLandlordsTask.firstStep }
                    checkable()
                    saveProgress()
                }
                task(journey.jointLandlordsTask) {
                    parents { journey.occupationTask.isComplete() }
                    nextStep { journey.cyaStep }
                    checkable()
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.checkAndSubmit.heading")
                step(journey.cyaStep) {
                    routeSegment("check-answers")
                    parents { journey.jointLandlordsTask.isComplete() }
                    nextUrl { "$PROPERTY_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
                }
            }
            checkYourAnswersJourney()
        }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeState(user)
}

@JourneyFrameworkComponent
class PropertyRegistrationJourney(
    // Task list step
    override val taskListStep: PropertyRegistrationTaskListStep,
    // Address task
    override val addressTask: PropertyRegistrationAddressTask,
    override val lookupAddressStep: LookupAddressStep,
    override val selectAddressStep: SelectAddressStep,
    override val alreadyRegisteredStep: AlreadyRegisteredStep,
    override val noAddressFoundStep: NoAddressFoundStep,
    override val manualAddressStep: ManualAddressStep,
    override val localCouncilStep: LocalCouncilStep,
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
    override val billsIncluded: BillsIncludedStep,
    override val furnishedStatus: FurnishedStatusStep,
    override val rentFrequency: RentFrequencyStep,
    override val rentAmount: RentAmountStep,
    // Joint landlords task
    override val jointLandlordsTask: JointLandlordsTask,
    override val hasAnyJointLandlordsInvitedStep: HasAnyJointLandlordsInvitedStep,
    override val hasJointLandlordsStep: HasJointLandlordsStep,
    override val inviteJointLandlordStep: InviteJointLandlordStep,
    override val inviteAnotherJointLandlordStep: InviteJointLandlordStep,
    override val removeJointLandlordStep: RemoveJointLandlordStep,
    override val checkJointLandlordsStep: CheckJointLandlordsStep,
    // Check your answers step
    override val cyaStep: PropertyRegistrationCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    PropertyRegistrationJourneyState {
    private var delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var cachedAddresses: List<AddressDataModel>? by delegateProvider.nullableDelegate("cachedAddresses")
    override var isAddressAlreadyRegistered: Boolean? by delegateProvider.nullableDelegate("isAddressAlreadyRegistered")
    override var cyaChildJourneyIdIfInitialized: String? by delegateProvider.nullableDelegate("checkYourAnswersChildJourneyId")
    override var invitedJointLandlordEmailsMap: Map<Int, String>? by delegateProvider.nullableDelegate("invitedJointLandlordEmails")
    override var checkingAnswersFor: CheckableElements? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var realBaseJourneyIdForCya: String? by delegateProvider.nullableDelegate("realBaseJourneyIdForCya")

    override fun generateJourneyId(seed: Any?): String {
        val user = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(user?.let { generateSeedForUser(it) })
    }

    companion object {
        fun generateSeedForUser(user: Principal): String = "Prop reg journey for user ${user.name} at time ${System.currentTimeMillis()}"
    }
}

interface PropertyRegistrationJourneyState :
    PropertyRegistrationAddressState,
    LicensingState,
    OccupationState,
    JointLandlordsState,
    CheckYourAnswersJourneyState {
    val taskListStep: PropertyRegistrationTaskListStep
    val addressTask: PropertyRegistrationAddressTask
    val propertyTypeStep: PropertyTypeStep
    val ownershipTypeStep: OwnershipTypeStep
    val licensingTask: LicensingTask
    val occupationTask: OccupationTask
    val jointLandlordsTask: JointLandlordsTask
    val finishCyaStep: FinishCyaJourneyStep
    override val cyaStep: PropertyRegistrationCyaStep

    var realBaseJourneyIdForCya: String?
    var checkingAnswersFor: CheckableElements?
}

enum class CheckableElements {
    PROPERTY_TYPE,
    OWNERSHIP_TYPE,
    LICENSING,
    OCCUPATION,
    HOUSEHOLDS,
    TENANTS,
    RENT_LEVELS,
    JOINT_LANDLORDS,
}
