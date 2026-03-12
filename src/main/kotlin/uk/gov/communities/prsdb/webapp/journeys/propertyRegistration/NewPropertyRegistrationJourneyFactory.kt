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
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyRegistrationAddressState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckAutomatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSuperseededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyJointLandlordsInvitedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalCouncilStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationCyaStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideEpcLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideGasCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveElectricalCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveGasCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveJointLandlordAreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseholdsAndTenantsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.JointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OccupationTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyRegistrationAddressTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentFrequencyAndAmountTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentIncludesBillsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
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

        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state)
        } else {
            checkYourAnswersJourneyMap(state, checkingAnswersFor)
        }
    }

    private fun checkYourAnswersJourneyMap(
        state: PropertyRegistrationJourneyState,
        checkingAnswersFor: String,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepDestination { journey.returnToCyaPageDestination }
            configure {
                withAdditionalContentProperty { "title" to "registerProperty.title" }
            }
            configureFirst {
                backDestination { journey.returnToCyaPageDestination }
            }

            when (checkingAnswersFor) {
                LookupAddressStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.addressTask)
                }

                "local-council" -> {
                    checkAnswerStep(journey.localCouncilStep, "local-council")
                }

                "property-type" -> {
                    checkAnswerStep(journey.propertyTypeStep, "property-type")
                }

                "ownership-type" -> {
                    checkAnswerStep(journey.ownershipTypeStep, "ownership-type")
                }

                "licensing-type" -> {
                    checkAnswerTask(journey.licensingTask)
                }

                "occupancy" -> {
                    checkAnswerTask(journey.occupationTask)
                }

                "number-of-households", "number-of-people" -> {
                    step(journey.households) {
                        initialStep()
                        nextStep { journey.tenants }
                        routeSegment("number-of-households")
                    }
                    step(journey.tenants) {
                        parents { journey.households.isComplete() }
                        routeSegment("number-of-people")
                        nextStep { journey.finishCyaStep }
                    }
                }

                "number-of-bedrooms" -> {
                    checkAnswerStep(journey.bedrooms, "number-of-bedrooms")
                }

                "rent-includes-bills" -> {
                    step(journey.rentIncludesBills) {
                        initialStep()
                        nextStep { mode ->
                            when (mode) {
                                YesOrNo.YES -> journey.billsIncluded
                                YesOrNo.NO -> journey.finishCyaStep
                            }
                        }
                        routeSegment("rent-includes-bills")
                    }
                    step(journey.billsIncluded) {
                        parents { journey.rentIncludesBills.hasOutcome(YesOrNo.YES) }
                        routeSegment("bills-included")
                        nextStep { journey.finishCyaStep }
                    }
                }

                "bills-included" -> {
                    checkAnswerStep(journey.billsIncluded, "bills-included")
                }

                "property-furnished" -> {
                    checkAnswerStep(journey.furnishedStatus, "property-furnished")
                }

                "rent-frequency" -> {
                    step(journey.rentFrequency) {
                        initialStep()
                        nextStep { journey.rentAmount }
                        routeSegment("rent-frequency")
                    }
                    step(journey.rentAmount) {
                        parents { journey.rentFrequency.isComplete() }
                        routeSegment("rent-amount")
                        nextStep { journey.finishCyaStep }
                    }
                }

                "rent-amount" -> {
                    checkAnswerStep(journey.rentAmount, "rent-amount")
                }

                "check-joint-landlords" -> {
                    checkAnswerTask(journey.jointLandlordsTask)
                }

                else -> {
                    throw IllegalStateException("Unknown checkable element $checkingAnswersFor")
                }
            }
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
        }

    private fun mainJourneyMap(state: PropertyRegistrationJourneyState): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepStep { journey.taskListStep }
            configure {
                withAdditionalContentProperty { "title" to "registerProperty.title" }
            }
            configureStep(journey.checkGasSafetyAnswersStep) {
                withAdditionalContentProperty { "sectionHeaderInfo" to null }
            }
            configureStep(journey.checkElectricalSafetyAnswersStep) {
                withAdditionalContentProperty { "sectionHeaderInfo" to null }
            }
            configureStep(journey.checkEpcAnswersStep) {
                withAdditionalContentProperty { "sectionHeaderInfo" to null }
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
                }
                step(journey.propertyTypeStep) {
                    routeSegment(PropertyTypeStep.ROUTE_SEGMENT)
                    parents { journey.addressTask.isComplete() }
                    nextStep { journey.ownershipTypeStep }
                    saveProgress()
                }
                step(journey.ownershipTypeStep) {
                    routeSegment(OwnershipTypeStep.ROUTE_SEGMENT)
                    parents { journey.propertyTypeStep.isComplete() }
                    nextStep { journey.licensingTask.firstStep }
                    saveProgress()
                }
                task(journey.licensingTask) {
                    parents { journey.ownershipTypeStep.isComplete() }
                    nextStep { journey.occupationTask.firstStep }
                    saveProgress()
                }
                task(journey.occupationTask) {
                    parents { journey.licensingTask.isComplete() }
                    nextStep { journey.jointLandlordsTask.firstStep }
                    saveProgress()
                }
                task(journey.jointLandlordsTask) {
                    parents { journey.occupationTask.isComplete() }
                    nextStep { journey.gasSafetyTask.firstStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.gasSafety", shouldUseNumbering = false)
                task(journey.gasSafetyTask) {
                    parents { journey.jointLandlordsTask.isComplete() }
                    nextStep { journey.electricalSafetyTask.firstStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.electricalSafety", shouldUseNumbering = false)
                task(journey.electricalSafetyTask) {
                    parents { journey.gasSafetyTask.isComplete() }
                    nextStep { journey.epcTask.firstStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.epc", shouldUseNumbering = false)
                task(journey.epcTask) {
                    parents { journey.electricalSafetyTask.isComplete() }
                    nextStep { journey.cyaStep }
                    saveProgress()
                }
            }
            section {
                withHeadingMessageKey("registerProperty.taskList.checkAndSubmit.heading")
                step(journey.cyaStep) {
                    routeSegment(PropertyRegistrationCyaStep.ROUTE_SEGMENT)
                    parents { journey.epcTask.isComplete() }
                    nextUrl { "$PROPERTY_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
                }
            }
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
    // Nested households and tenants task
    override val householdsAndTenantsTask: HouseholdsAndTenantsTask,
    override val households: HouseholdStep,
    override val tenants: TenantsStep,
    override val bedrooms: BedroomsStep,
    // Nested rent includes bills task
    override val rentIncludesBillsTask: RentIncludesBillsTask,
    override val rentIncludesBills: RentIncludesBillsStep,
    override val billsIncluded: BillsIncludedStep,
    override val furnishedStatus: FurnishedStatusStep,
    // Nested rent frequency and amount task
    override val rentFrequencyAndAmountTask: RentFrequencyAndAmountTask,
    override val rentFrequency: RentFrequencyStep,
    override val rentAmount: RentAmountStep,
    // Joint landlords task
    override val jointLandlordsTask: JointLandlordsTask,
    override val hasAnyJointLandlordsInvitedStep: HasAnyJointLandlordsInvitedStep,
    override val hasJointLandlordsStep: HasJointLandlordsStep,
    override val inviteJointLandlordStep: InviteJointLandlordStep,
    override val inviteAnotherJointLandlordStep: InviteJointLandlordStep,
    override val removeJointLandlordAreYouSureStep: RemoveJointLandlordAreYouSureStep,
    override val checkJointLandlordsStep: CheckJointLandlordsStep,
    // Gas safety task
    override val gasSafetyTask: GasSafetyTask,
    override val hasGasSupplyStep: HasGasSupplyStep,
    override val hasGasCertStep: HasGasCertStep,
    override val gasCertIssueDateStep: GasCertIssueDateStep,
    override val uploadGasCertStep: UploadGasCertStep,
    override val checkGasCertUploadsStep: CheckGasCertUploadsStep,
    override val removeGasCertUploadStep: RemoveGasCertUploadStep,
    override val gasCertExpiredStep: GasCertExpiredStep,
    override val gasCertMissingStep: GasCertMissingStep,
    override val provideGasCertLaterStep: ProvideGasCertLaterStep,
    override val checkGasSafetyAnswersStep: CheckGasSafetyAnswersStep,
    // Electrical safety task
    override val electricalSafetyTask: ElectricalSafetyTask,
    override val hasElectricalCertStep: HasElectricalCertStep,
    override val electricalCertIssueDateStep: ElectricalCertIssueDateStep,
    override val uploadElectricalCertStep: UploadElectricalCertStep,
    override val checkElectricalCertUploadsStep: CheckElectricalCertUploadsStep,
    override val removeElectricalCertUploadStep: RemoveElectricalCertUploadStep,
    override val electricalCertExpiredStep: ElectricalCertExpiredStep,
    override val electricalCertMissingStep: ElectricalCertMissingStep,
    override val provideElectricalCertLaterStep: ProvideElectricalCertLaterStep,
    override val checkElectricalSafetyAnswersStep: CheckElectricalSafetyAnswersStep,
    // EPC task
    override val epcTask: EpcTask,
    override val hasEpcStep: HasEpcStep,
    override val checkAutomatchedEpcStep: CheckAutomatchedEpcStep,
    override val epcSearchStep: EpcSearchStep,
    override val epcSuperseededStep: EpcSuperseededStep,
    override val epcNotFoundStep: EpcNotFoundStep,
    override val checkMatchedEpcStep: CheckMatchedEpcStep,
    override val epcExpiryCheckStep: EpcExpiryCheckStep,
    override val hasMeesExemptionStep: HasMeesExemptionStep,
    override val meesExemptionStep: MeesExemptionStep,
    override val lowEnergyRatingStep: LowEnergyRatingStep,
    override val epcExpiredStep: EpcExpiredStep,
    override val hasEpcExemptionStep: HasEpcExemptionStep,
    override val epcExemptionStep: EpcExemptionStep,
    override val epcMissingStep: EpcMissingStep,
    override val provideEpcLaterStep: ProvideEpcLaterStep,
    override val checkEpcAnswersStep: CheckEpcAnswersStep,
    // Check your answers step
    override val cyaStep: PropertyRegistrationCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    journeyStateService: JourneyStateService,
    override val stateFactory: ObjectFactory<PropertyRegistrationJourneyState>,
) : AbstractJourneyState(journeyStateService),
    PropertyRegistrationJourneyState {
    private var delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var cachedAddresses: List<AddressDataModel>? by delegateProvider.nullableDelegate("cachedAddresses")
    override var isAddressAlreadyRegistered: Boolean? by delegateProvider.nullableDelegate("isAddressAlreadyRegistered")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var invitedJointLandlordEmailsMap: Map<Int, String>? by delegateProvider.nullableDelegate("invitedJointLandlordEmails")
    override var nextJointLandlordMemberId: Int? by delegateProvider.nullableDelegate("nextJointLandlordMemberId")
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")

    override var cyaRouteSegment: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override val isOccupied: Boolean? get() = occupied.formModelOrNull?.occupied

    override fun generateJourneyId(seed: Any?): String {
        val user = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(user?.let { generateSeedForUser(it) } ?: seed)
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
    GasSafetyState,
    ElectricalSafetyState,
    EpcState,
    CheckYourAnswersJourneyState {
    val taskListStep: PropertyRegistrationTaskListStep
    val addressTask: PropertyRegistrationAddressTask
    val propertyTypeStep: PropertyTypeStep
    val ownershipTypeStep: OwnershipTypeStep
    val licensingTask: LicensingTask
    val occupationTask: OccupationTask
    val jointLandlordsTask: JointLandlordsTask
    override val finishCyaStep: FinishCyaJourneyStep
    val gasSafetyTask: GasSafetyTask
    val electricalSafetyTask: ElectricalSafetyTask
    val epcTask: EpcTask
    override val cyaStep: PropertyRegistrationCyaStep
}
