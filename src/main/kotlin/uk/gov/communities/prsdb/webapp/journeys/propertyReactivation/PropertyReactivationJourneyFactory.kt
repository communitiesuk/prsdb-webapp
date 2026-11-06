package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.SingleParent
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.ELECTRICAL_SAFETY
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.EPC
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.GAS_SAFETY
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.LICENSING
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationTaskType.TENANCY_DETAILS
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationCheckAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationElectricalSafetyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationGasSafetyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationLicensingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationTenancyDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.initialiseFromPropertyOwnershipId
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class PropertyReactivationJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyReactivationJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    fun createJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = getInitializedState(propertyOwnershipId)

        return journey(state) {
            unreachableStepStep { journey.infoStep }
            step(journey.infoStep) {
                routeSegment(ReactivationInfoStep.ROUTE_SEGMENT)
                initialStep()
                // TODO PDJB-2034: go back to the deactivated property record
                backUrl { LANDLORD_DASHBOARD_URL }
                nextStep { journey.stepAfterInfo() }
            }
            // TODO PDJB-2037: if this becomes a task, register it with task(...) and update entryStepFor and completionStepFor
            step(journey.licensingStep) {
                routeSegment(ReactivationLicensingStep.ROUTE_SEGMENT)
                parents { journey.parentsFor(LICENSING) }
                nextStep { journey.requiredStepAfter(LICENSING) }
            }
            // TODO PDJB-2038: if this becomes a task, register it with task(...) and update entryStepFor and completionStepFor
            step(journey.gasSafetyStep) {
                routeSegment(ReactivationGasSafetyStep.ROUTE_SEGMENT)
                parents { journey.parentsFor(GAS_SAFETY) }
                nextStep { journey.requiredStepAfter(GAS_SAFETY) }
            }
            // TODO PDJB-2039: if this becomes a task, register it with task(...) and update entryStepFor and completionStepFor
            step(journey.electricalSafetyStep) {
                routeSegment(ReactivationElectricalSafetyStep.ROUTE_SEGMENT)
                parents { journey.parentsFor(ELECTRICAL_SAFETY) }
                nextStep { journey.requiredStepAfter(ELECTRICAL_SAFETY) }
            }
            // TODO PDJB-2040: if this becomes a task, register it with task(...) and update entryStepFor and completionStepFor
            step(journey.epcStep) {
                routeSegment(ReactivationEpcStep.ROUTE_SEGMENT)
                parents { journey.parentsFor(EPC) }
                nextStep { journey.requiredStepAfter(EPC) }
            }
            // TODO PDJB-2041: if this becomes a task, register it with task(...) and update entryStepFor and completionStepFor
            step(journey.tenancyDetailsStep) {
                routeSegment(ReactivationTenancyDetailsStep.ROUTE_SEGMENT)
                parents { journey.parentsFor(TENANCY_DETAILS) }
                nextStep { journey.requiredStepAfter(TENANCY_DETAILS) }
            }
            step(journey.checkAnswersStep) {
                routeSegment(ReactivationCheckAnswersStep.ROUTE_SEGMENT)
                parents { journey.checkAnswersParents() }
                nextUrl {
                    "${ReactivatePropertyController.getPropertyReactivationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT"
                }
            }
        }
    }

    private fun getInitializedState(propertyOwnershipId: Long): PropertyReactivationJourney {
        val state = stateFactory.getObject()
        if (!state.isStateInitialized) {
            val propertyOwnership = propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId)
            state.requiredTaskTypes = ReactivationTaskType.requiredFor(propertyOwnership)
        }
        return state.initialiseFromPropertyOwnershipId(propertyOwnershipId)
    }

    fun initializeJourneyState(propertyOwnershipId: Long): String = stateFactory.getObject().initializeState(propertyOwnershipId)

    private fun PropertyReactivationJourneyState.entryStepFor(taskType: ReactivationTaskType): JourneyStep<*, *, *> =
        when (taskType) {
            LICENSING -> licensingStep
            GAS_SAFETY -> gasSafetyStep
            ELECTRICAL_SAFETY -> electricalSafetyStep
            EPC -> epcStep
            TENANCY_DETAILS -> tenancyDetailsStep
        }

    // The step whose outcome means the user has finished this part of the journey, e.g. a task's exit step
    private fun PropertyReactivationJourneyState.completionStepFor(taskType: ReactivationTaskType): JourneyStep<*, *, *> =
        when (taskType) {
            LICENSING -> licensingStep
            GAS_SAFETY -> gasSafetyStep
            ELECTRICAL_SAFETY -> electricalSafetyStep
            EPC -> epcStep
            TENANCY_DETAILS -> tenancyDetailsStep
        }

    // Task types are compared by enum order, which is the order the tasks appear in the journey
    private fun PropertyReactivationJourneyState.requiredStepAfter(taskType: ReactivationTaskType): JourneyStep<*, *, *> =
        requiredTaskTypes.filter { it > taskType }.minOrNull()?.let { entryStepFor(it) } ?: checkAnswersStep

    private fun PropertyReactivationJourneyState.stepAfterInfo(): JourneyStep<*, *, *> =
        requiredTaskTypes.minOrNull()?.let { entryStepFor(it) } ?: checkAnswersStep

    // A step the property doesn't need is never reachable, so the user is sent back to the info step
    private fun PropertyReactivationJourneyState.parentsFor(taskType: ReactivationTaskType): Parentage {
        val previousStep = requiredTaskTypes.filter { it < taskType }.maxOrNull()?.let { completionStepFor(it) } ?: infoStep
        return SingleParent(previousStep) { taskType in requiredTaskTypes && previousStep.outcome != null }
    }

    private fun PropertyReactivationJourneyState.checkAnswersParents(): Parentage {
        val previousStep = requiredTaskTypes.maxOrNull()?.let { completionStepFor(it) } ?: infoStep
        return SingleParent(previousStep) { previousStep.outcome != null }
    }
}

@JourneyFrameworkComponent
class PropertyReactivationJourney(
    override val infoStep: ReactivationInfoStep,
    override val licensingStep: ReactivationLicensingStep,
    override val gasSafetyStep: ReactivationGasSafetyStep,
    override val electricalSafetyStep: ReactivationElectricalSafetyStep,
    override val epcStep: ReactivationEpcStep,
    override val tenancyDetailsStep: ReactivationTenancyDetailsStep,
    override val checkAnswersStep: ReactivationCheckAnswersStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    PropertyReactivationJourneyState {
    override var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var propertyOwnershipId: Long by delegateProvider.requiredImmutableDelegate("propertyOwnershipId")
    override var requiredTaskTypes: List<ReactivationTaskType> by delegateProvider.requiredImmutableDelegate("requiredTaskTypes")

    override fun generateJourneyId(seed: Any?): String {
        val propertyOwnershipId = seed as? Long
        return super<AbstractJourneyState>.generateJourneyId(
            propertyOwnershipId?.let { generateSeedForPropertyOwnership(it) },
        )
    }

    companion object {
        private fun generateSeedForPropertyOwnership(propertyOwnershipId: Long): String =
            "Property reactivation journey for property $propertyOwnershipId at time ${System.currentTimeMillis()}"
    }
}

interface PropertyReactivationJourneyState : PropertyOwnershipJourneyState {
    val infoStep: ReactivationInfoStep
    val licensingStep: ReactivationLicensingStep
    val gasSafetyStep: ReactivationGasSafetyStep
    val electricalSafetyStep: ReactivationElectricalSafetyStep
    val epcStep: ReactivationEpcStep
    val tenancyDetailsStep: ReactivationTenancyDetailsStep
    val checkAnswersStep: ReactivationCheckAnswersStep
    var requiredTaskTypes: List<ReactivationTaskType>
}
