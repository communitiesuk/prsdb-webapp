package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.SingleParent
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.ELECTRICAL_SAFETY
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.EPC
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.GAS_SAFETY
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.LICENSING
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.TENANCY_DETAILS
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.CompleteReactivationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationAlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationCheckAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationConfirmMissingComplianceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoMode
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicrUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.IsEpcRequiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.StartEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseholdsAndTenantsDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.TenancyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.hideCheckAnswersSectionHeader
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.useContinueButtons
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTaskFrom
import uk.gov.communities.prsdb.webapp.journeys.shared.states.MissingComplianceState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.initialiseFromPropertyOwnershipId
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ConfirmMissingComplianceCheckResult
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ConfirmMissingComplianceMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.HasMissingComplianceStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.HasMissingComplianceStepConfig
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class PropertyReactivationJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyReactivationJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    fun createJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = getInitializedState(propertyOwnershipId)

        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state)
        } else {
            checkYourAnswersJourneyMap(state, checkingAnswersFor)
        }
    }

    private fun mainJourneyMap(state: PropertyReactivationJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepStep { journey.infoStep }
            step(journey.infoStep) {
                routeSegment(ReactivationInfoStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { PropertyDetailsController.getPropertyDetailsPath(journey.propertyOwnershipId) }
                nextStep { mode ->
                    when (mode) {
                        ReactivationInfoMode.PROPERTY_AVAILABLE -> journey.stepAfterInfo()
                        ReactivationInfoMode.PROPERTY_ALREADY_REGISTERED -> journey.alreadyRegisteredStep
                    }
                }
            }
            step(journey.alreadyRegisteredStep) {
                routeSegment(ReactivationAlreadyRegisteredStep.ROUTE_SEGMENT)
                parents { journey.infoStep.hasOutcome(ReactivationInfoMode.PROPERTY_ALREADY_REGISTERED) }
                noNextDestination()
            }
            section {
                withHeadingMessageKey("licensing.sectionHeading", shouldUseNumbering = false)
                task(journey.licensingTask) {
                    withDependencies { journey }
                    parents { journey.parentsFor(LICENSING) }
                    nextStep { journey.nextStepAfter(LICENSING) }
                }
            }
            section {
                withHeadingMessageKey("propertyCompliance.gasSafetyTask.sectionHeading", shouldUseNumbering = false)
                task(journey.gasSafetyTask) {
                    withDependencies { journey }
                    parents { journey.parentsFor(GAS_SAFETY) }
                    nextStep { journey.nextStepAfter(GAS_SAFETY) }
                }
            }
            section {
                withHeadingMessageKey("propertyCompliance.electricalSafetyTask.sectionHeading", shouldUseNumbering = false)
                task(journey.electricalSafetyTask) {
                    withDependencies { journey }
                    parents { journey.parentsFor(ELECTRICAL_SAFETY) }
                    nextStep { journey.nextStepAfter(ELECTRICAL_SAFETY) }
                }
            }
            section {
                withHeadingMessageKey("propertyCompliance.epcTask.sectionHeading", shouldUseNumbering = false)
                task(journey.epcTask) {
                    withDependencies { journey }
                    parents { journey.parentsFor(EPC) }
                    nextStep { journey.nextStepAfter(EPC) }
                }
            }
            section {
                withHeadingMessageKey("tenancyDetails.sectionHeading", shouldUseNumbering = false)
                task(journey.tenancyDetailsTask) {
                    withDependencies { journey }
                    parents { journey.parentsFor(TENANCY_DETAILS) }
                    nextStep { journey.nextStepAfter(TENANCY_DETAILS) }
                }
            }
            step(journey.checkAnswersStep) {
                routeSegment(ReactivationCheckAnswersStep.ROUTE_SEGMENT)
                parents { journey.checkAnswersParents() }
                nextStep { journey.hasMissingComplianceStep }
            }
            step(journey.hasMissingComplianceStep) {
                parents { journey.checkAnswersStep.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES -> {
                            journey.confirmMissingComplianceStep
                        }

                        ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_VALID_CERTIFICATES_OR_DELEGATED -> {
                            journey.completeReactivationStep
                        }
                    }
                }
            }
            step(journey.confirmMissingComplianceStep) {
                routeSegment(ReactivationConfirmMissingComplianceStep.ROUTE_SEGMENT)
                parents {
                    journey.hasMissingComplianceStep.hasOutcome(
                        ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES,
                    )
                }
                nextDestination { mode ->
                    when (mode) {
                        ConfirmMissingComplianceMode.GO_BACK -> Destination(journey.checkAnswersStep)
                        ConfirmMissingComplianceMode.CONFIRMED -> Destination(journey.completeReactivationStep)
                    }
                }
            }
            step(journey.completeReactivationStep) {
                parents { journey.completeReactivationParents() }
                nextUrl { confirmationUrl(journey.propertyOwnershipId) }
            }
            useContinueButtonsForTasks()
            hideCheckAnswersSectionHeader(journey.gasSafetyTask)
            hideCheckAnswersSectionHeader(journey.electricalSafetyTask)
            hideCheckAnswersSectionHeader(journey.epcTask)
        }

    private fun checkYourAnswersJourneyMap(
        state: PropertyReactivationJourney,
        checkingAnswersFor: String,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepDestination { journey.returnToCyaPageDestination }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            when (checkingAnswersFor) {
                LicensingTypeStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.licensingTask, { journey })
                }

                SelectiveLicenceStep.ROUTE_SEGMENT -> {
                    fromTask(journey.licensingTask, journey) {
                        checkAnswerStep(task.selectiveLicenceStep, SelectiveLicenceStep.ROUTE_SEGMENT)
                    }
                }

                HmoMandatoryLicenceStep.ROUTE_SEGMENT -> {
                    fromTask(journey.licensingTask, journey) {
                        checkAnswerStep(task.hmoMandatoryLicenceStep, HmoMandatoryLicenceStep.ROUTE_SEGMENT)
                    }
                }

                HmoAdditionalLicenceStep.ROUTE_SEGMENT -> {
                    fromTask(journey.licensingTask, journey) {
                        checkAnswerStep(task.hmoAdditionalLicenceStep, HmoAdditionalLicenceStep.ROUTE_SEGMENT)
                    }
                }

                HasGasSupplyStep.ROUTE_SEGMENT,
                CheckGasCertUploadsStep.ROUTE_SEGMENT,
                -> {
                    checkAnswerTask(journey.gasSafetyTask.gasSafetyDetailsTask, { journey })
                }

                HasGasCertStep.ROUTE_SEGMENT -> {
                    checkAnswerTaskFrom(
                        journey.gasSafetyTask.gasSafetyDetailsTask,
                        { journey },
                        startStep = journey.gasSafetyTask.gasSafetyDetailsTask.hasGasCertStep,
                    )
                }

                GasCertIssueDateStep.ROUTE_SEGMENT -> {
                    checkAnswerTaskFrom(
                        journey.gasSafetyTask.gasSafetyDetailsTask,
                        { journey },
                        startStep = journey.gasSafetyTask.gasSafetyDetailsTask.gasCertIssueDateStep,
                    )
                }

                HasElectricalCertStep.ROUTE_SEGMENT,
                CheckEicUploadsStep.ROUTE_SEGMENT,
                CheckEicrUploadsStep.ROUTE_SEGMENT,
                -> {
                    checkAnswerTask(journey.electricalSafetyTask.electricalSafetyDetailsTask, { journey })
                }

                // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
                EicExpiryDateStep.ROUTE_SEGMENT -> {
                    checkAnswerTaskFrom(
                        journey.electricalSafetyTask.electricalSafetyDetailsTask,
                        { journey },
                        startStep = journey.electricalSafetyTask.electricalSafetyDetailsTask.eicExpiryDateStep,
                    )
                }

                EicIssueDateStep.ROUTE_SEGMENT -> {
                    checkAnswerTaskFrom(
                        journey.electricalSafetyTask.electricalSafetyDetailsTask,
                        { journey },
                        startStep = journey.electricalSafetyTask.electricalSafetyDetailsTask.eicIssueDateStep,
                    )
                }

                EicrExpiryDateStep.ROUTE_SEGMENT -> {
                    checkAnswerTaskFrom(
                        journey.electricalSafetyTask.electricalSafetyDetailsTask,
                        { journey },
                        startStep = journey.electricalSafetyTask.electricalSafetyDetailsTask.eicrExpiryDateStep,
                    )
                }

                StartEpcStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.epcTask.epcDetailsTask, { journey })
                }

                HasEpcStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.epcTask.epcDetailsTask, { journey })
                    configureStep(journey.epcTask.epcDetailsTask.hasEpcStep) {
                        backDestination { journey.returnToCyaPageDestination }
                    }
                }

                EpcInDateAtStartOfTenancyCheckStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.epcTask.epcDetailsTask, { journey })
                    configureStep(journey.epcTask.epcDetailsTask.epcInDateAtStartOfTenancyCheckStep) {
                        backDestination { journey.returnToCyaPageDestination }
                    }
                }

                IsEpcRequiredStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.epcTask.epcDetailsTask, { journey })
                    configureStep(journey.epcTask.epcDetailsTask.isEpcRequiredStep) {
                        backDestination { journey.returnToCyaPageDestination }
                    }
                }

                EpcExemptionStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.epcTask.epcDetailsTask, { journey })
                    configureStep(journey.epcTask.epcDetailsTask.epcExemptionStep) {
                        backDestination { journey.returnToCyaPageDestination }
                    }
                }

                HasMeesExemptionStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.epcTask.epcDetailsTask, { journey })
                    configureStep(journey.epcTask.epcDetailsTask.hasMeesExemptionStep) {
                        backDestination { journey.returnToCyaPageDestination }
                    }
                }

                MeesExemptionStep.ROUTE_SEGMENT -> {
                    fromTask(journey.epcTask.epcDetailsTask, journey) {
                        checkAnswerStep(task.meesExemptionStep, MeesExemptionStep.ROUTE_SEGMENT)
                    }
                }

                HouseholdStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.tenancyDetailsTask.householdsAndTenantsTask, { journey })
                }

                TenantsStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.tenancyDetailsTask.householdsAndTenantsTask, { journey })
                    configureStep(journey.tenancyDetailsTask.householdsAndTenantsTask.tenants) {
                        backDestination { journey.returnToCyaPageDestination }
                    }
                }

                RentIncludesBillsStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.tenancyDetailsTask.rentIncludesBillsTask)
                }

                BillsIncludedStep.ROUTE_SEGMENT -> {
                    fromTask(journey.tenancyDetailsTask.rentIncludesBillsTask) {
                        checkAnswerStep(task.billsIncluded, BillsIncludedStep.ROUTE_SEGMENT)
                    }
                }

                FurnishedStatusStep.ROUTE_SEGMENT -> {
                    fromTask(journey.tenancyDetailsTask, journey) {
                        checkAnswerStep(task.furnishedStatus, FurnishedStatusStep.ROUTE_SEGMENT)
                    }
                }

                RentFrequencyStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.tenancyDetailsTask.rentFrequencyAndAmountTask)
                }

                RentAmountStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.tenancyDetailsTask.rentFrequencyAndAmountTask)
                    configureStep(journey.tenancyDetailsTask.rentFrequencyAndAmountTask.rentAmount) {
                        backDestination { journey.returnToCyaPageDestination }
                    }
                }

                else -> {
                    throw IllegalStateException("Unknown checkable element $checkingAnswersFor")
                }
            }
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
            useContinueButtonsForTasks()
        }

    private fun JourneyBuilder<PropertyReactivationJourney>.useContinueButtonsForTasks() {
        useContinueButtons(journey.licensingTask)
        useContinueButtons(journey.gasSafetyTask)
        useContinueButtons(journey.electricalSafetyTask)
        useContinueButtons(journey.epcTask)
        useContinueButtons(journey.tenancyDetailsTask)
    }

    private fun getInitializedState(propertyOwnershipId: Long): PropertyReactivationJourney {
        val state = stateFactory.getObject()
        if (!state.isStateInitialized) {
            val propertyOwnership = propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId)
            state.requiredTaskTypes = ReactivationTaskType.requiredFor(propertyOwnership)
            state.isOccupied = propertyOwnership.isOccupied
            state.uprn = propertyOwnership.address.uprn
            state.previousRenewalDate = propertyOwnership.renewalDate.toKotlinLocalDate()
        }
        return state.initialiseFromPropertyOwnershipId(propertyOwnershipId)
    }

    fun initializeJourneyState(propertyOwnershipId: Long): String = stateFactory.getObject().initializeState(propertyOwnershipId)

    private fun PropertyReactivationJourneyState.entryStepFor(taskType: ReactivationTaskType): JourneyStep<*, *, *> =
        when (taskType) {
            LICENSING -> licensingTask.firstStep
            GAS_SAFETY -> gasSafetyTask.firstStep
            ELECTRICAL_SAFETY -> electricalSafetyTask.firstStep
            EPC -> epcTask.firstStep
            TENANCY_DETAILS -> tenancyDetailsTask.firstStep
        }

    private fun PropertyReactivationJourneyState.completionStepFor(taskType: ReactivationTaskType): JourneyStep<*, *, *> =
        when (taskType) {
            LICENSING -> licensingTask.exitStep
            GAS_SAFETY -> gasSafetyTask.exitStep
            ELECTRICAL_SAFETY -> electricalSafetyTask.exitStep
            EPC -> epcTask.exitStep
            TENANCY_DETAILS -> tenancyDetailsTask.exitStep
        }

    private fun PropertyReactivationJourneyState.nextStepAfter(taskType: ReactivationTaskType): JourneyStep<*, *, *> =
        requiredTaskTypes.filter { it > taskType }.minOrNull()?.let { entryStepFor(it) } ?: checkAnswersStep

    private fun PropertyReactivationJourneyState.stepAfterInfo(): JourneyStep<*, *, *> =
        requiredTaskTypes.minOrNull()?.let { entryStepFor(it) } ?: completeReactivationStep

    private fun confirmationUrl(propertyOwnershipId: Long) =
        "${ReactivatePropertyController.getPropertyReactivationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT"

    private fun PropertyReactivationJourneyState.parentsFor(taskType: ReactivationTaskType): Parentage {
        val previousStep = requiredTaskTypes.filter { it < taskType }.maxOrNull()?.let { completionStepFor(it) }
        return if (previousStep == null) {
            SingleParent(infoStep) { taskType in requiredTaskTypes && infoStep.outcome == ReactivationInfoMode.PROPERTY_AVAILABLE }
        } else {
            SingleParent(previousStep) { taskType in requiredTaskTypes && previousStep.outcome != null }
        }
    }

    private fun PropertyReactivationJourneyState.checkAnswersParents(): Parentage {
        val previousStep =
            requiredTaskTypes.maxOrNull()?.let { completionStepFor(it) }
                ?: return SingleParent(infoStep) { false }
        return SingleParent(previousStep) { previousStep.outcome != null }
    }

    private fun PropertyReactivationJourneyState.completeReactivationParents(): Parentage =
        OrParents(
            SingleParent(infoStep) { requiredTaskTypes.isEmpty() && infoStep.outcome == ReactivationInfoMode.PROPERTY_AVAILABLE },
            hasMissingComplianceStep.hasOutcome(ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_VALID_CERTIFICATES_OR_DELEGATED),
            confirmMissingComplianceStep.hasOutcome(ConfirmMissingComplianceMode.CONFIRMED),
        )
}

@JourneyFrameworkComponent
class PropertyReactivationJourney(
    override val infoStep: ReactivationInfoStep,
    override val alreadyRegisteredStep: ReactivationAlreadyRegisteredStep,
    override val licensingTask: LicensingTask,
    override val gasSafetyTask: GasSafetyTask,
    override val electricalSafetyTask: ElectricalSafetyTask,
    override val epcTask: EpcTask,
    override val tenancyDetailsTask: TenancyDetailsTask,
    override val checkAnswersStep: ReactivationCheckAnswersStep,
    override val hasMissingComplianceStep: HasMissingComplianceStep,
    override val confirmMissingComplianceStep: ReactivationConfirmMissingComplianceStep,
    override val completeReactivationStep: CompleteReactivationStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    journeyStateService: JourneyStateService,
    override val stateFactory: ObjectFactory<PropertyReactivationJourneyState>,
) : AbstractJourneyState(journeyStateService),
    PropertyReactivationJourneyState {
    override var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var propertyOwnershipId: Long by delegateProvider.requiredImmutableDelegate("propertyOwnershipId")
    override var requiredTaskTypes: List<ReactivationTaskType> by delegateProvider.requiredImmutableDelegate("requiredTaskTypes")
    override var isOccupied: Boolean by delegateProvider.requiredImmutableDelegate("isOccupied")
    override var uprn: Long? by delegateProvider.nullableDelegate("uprn")
    override var previousRenewalDate: LocalDate by delegateProvider.requiredImmutableDelegate("previousRenewalDate")
    override val allowProvideCertificateLaterRoute: Boolean = false
    override val allowProvideTenancyDetailsLaterRoute: Boolean = false
    override val allowProvideLicensingLaterRoute: Boolean = false
    override var isPropertyAlreadyRegistered: Boolean? by delegateProvider.nullableDelegate("isPropertyAlreadyRegistered")

    override val cyaStep
        get() = checkAnswersStep
    override var cyaJourneys: Map<String, String> = mapOf()
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override fun generateJourneyId(seed: Any?): String {
        val propertyOwnershipId = seed as? Long
        return super<AbstractJourneyState>.generateJourneyId(
            propertyOwnershipId?.let { generateSeedForPropertyOwnership(it) } ?: seed,
        )
    }

    companion object {
        private fun generateSeedForPropertyOwnership(propertyOwnershipId: Long): String =
            "Property reactivation journey for property $propertyOwnershipId at time ${System.currentTimeMillis()}"
    }
}

interface PropertyReactivationJourneyState :
    PropertyOwnershipJourneyState,
    LicensingDependencies,
    GasSafetyDependencies,
    ElectricalSafetyDependencies,
    EpcDependencies,
    HouseholdsAndTenantsDependencies,
    MissingComplianceState {
    val infoStep: ReactivationInfoStep
    val alreadyRegisteredStep: ReactivationAlreadyRegisteredStep
    val licensingTask: LicensingTask
    val gasSafetyTask: GasSafetyTask
    val electricalSafetyTask: ElectricalSafetyTask
    val epcTask: EpcTask
    val tenancyDetailsTask: TenancyDetailsTask
    val checkAnswersStep: ReactivationCheckAnswersStep
    val hasMissingComplianceStep: HasMissingComplianceStep
    val confirmMissingComplianceStep: ReactivationConfirmMissingComplianceStep
    val completeReactivationStep: CompleteReactivationStep
    var requiredTaskTypes: List<ReactivationTaskType>
    override var propertyOwnershipId: Long
    override var isOccupied: Boolean
    override var uprn: Long?
    var isPropertyAlreadyRegistered: Boolean?
    var previousRenewalDate: LocalDate

    override val isMissingComplianceCheckApplicable: Boolean
        get() = isOccupied
    override val isGasCertMissing: Boolean
        get() = GAS_SAFETY in requiredTaskTypes && HasMissingComplianceStepConfig.isGasCertInvalid(gasSafetyTask)
    override val isElectricalCertMissing: Boolean
        get() = ELECTRICAL_SAFETY in requiredTaskTypes && HasMissingComplianceStepConfig.isElectricalCertInvalid(electricalSafetyTask)
    override val isEpcMissing: Boolean
        get() = EPC in requiredTaskTypes && HasMissingComplianceStepConfig.isEpcInvalid(epcTask)
}
