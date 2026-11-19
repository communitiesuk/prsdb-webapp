package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.SingleParent
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
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
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoMode
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
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
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.useCertDateBackStepsFromCheckUploads
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.useContinueButtons
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.initialiseFromPropertyOwnershipId
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class PropertyReactivationJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyReactivationJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    // TODO PDJB-2042: build a check your answers child journey map when state.checkingAnswersFor is set
    fun createJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = getInitializedState(propertyOwnershipId)

        return journey(state) {
            unreachableStepStep { journey.infoStep }
            step(journey.infoStep) {
                routeSegment(ReactivationInfoStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) }
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
                useCertDateBackStepsFromCheckUploads(journey.electricalSafetyTask.electricalSafetyDetailsTask)
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
                nextStep { journey.completeReactivationStep }
            }
            step(journey.completeReactivationStep) {
                parents { journey.completeReactivationParents() }
                nextUrl { confirmationUrl(propertyOwnershipId) }
            }
            useContinueButtons(journey.licensingTask)
            useContinueButtons(journey.gasSafetyTask)
            useContinueButtons(journey.electricalSafetyTask)
            useContinueButtons(journey.epcTask)
            useContinueButtons(journey.tenancyDetailsTask)
            hideCheckAnswersSectionHeader(journey.gasSafetyTask)
            hideCheckAnswersSectionHeader(journey.electricalSafetyTask)
            hideCheckAnswersSectionHeader(journey.epcTask)
        }
    }

    private fun getInitializedState(propertyOwnershipId: Long): PropertyReactivationJourney {
        val state = stateFactory.getObject()
        if (!state.isStateInitialized) {
            val propertyOwnership = propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId)
            state.requiredTaskTypes = ReactivationTaskType.requiredFor(propertyOwnership)
            state.isOccupied = propertyOwnership.isOccupied
            state.uprn = propertyOwnership.address.uprn
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
            checkAnswersStep.isComplete(),
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
    CheckYourAnswersJourneyState {
    val infoStep: ReactivationInfoStep
    val alreadyRegisteredStep: ReactivationAlreadyRegisteredStep
    val licensingTask: LicensingTask
    val gasSafetyTask: GasSafetyTask
    val electricalSafetyTask: ElectricalSafetyTask
    val epcTask: EpcTask
    val tenancyDetailsTask: TenancyDetailsTask
    val checkAnswersStep: ReactivationCheckAnswersStep
    val completeReactivationStep: CompleteReactivationStep
    var requiredTaskTypes: List<ReactivationTaskType>
    override var propertyOwnershipId: Long
    override var isOccupied: Boolean
    override var uprn: Long?
    var isPropertyAlreadyRegistered: Boolean?
}
