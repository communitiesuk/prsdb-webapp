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
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationStepType.ELECTRICAL_SAFETY
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationStepType.EPC
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationStepType.GAS_SAFETY
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationStepType.LICENSING
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.ReactivationStepType.TENANCY_DETAILS
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
        val requiredStepTypes = state.requiredStepTypes

        // Only the steps required for this property are included, so each step's neighbours depend on that selection
        val orderedSteps =
            listOf(state.infoStep) + requiredStepTypes.map { state.getStep(it) } + listOf(state.checkAnswersStep)

        fun stepBefore(step: JourneyStep<*, *, *>) = orderedSteps[orderedSteps.indexOf(step) - 1]

        fun stepAfter(step: JourneyStep<*, *, *>) = orderedSteps[orderedSteps.indexOf(step) + 1]

        return journey(state) {
            unreachableStepStep { journey.infoStep }
            step(journey.infoStep) {
                routeSegment(ReactivationInfoStep.ROUTE_SEGMENT)
                initialStep()
                // TODO PDJB-2034: go back to the deactivated property record
                backUrl { LANDLORD_DASHBOARD_URL }
                nextStep { stepAfter(journey.infoStep) }
            }
            if (LICENSING in requiredStepTypes) {
                step(journey.licensingStep) {
                    routeSegment(ReactivationLicensingStep.ROUTE_SEGMENT)
                    parents { stepBefore(journey.licensingStep).isComplete() }
                    nextStep { stepAfter(journey.licensingStep) }
                }
            }
            if (GAS_SAFETY in requiredStepTypes) {
                step(journey.gasSafetyStep) {
                    routeSegment(ReactivationGasSafetyStep.ROUTE_SEGMENT)
                    parents { stepBefore(journey.gasSafetyStep).isComplete() }
                    nextStep { stepAfter(journey.gasSafetyStep) }
                }
            }
            if (ELECTRICAL_SAFETY in requiredStepTypes) {
                step(journey.electricalSafetyStep) {
                    routeSegment(ReactivationElectricalSafetyStep.ROUTE_SEGMENT)
                    parents { stepBefore(journey.electricalSafetyStep).isComplete() }
                    nextStep { stepAfter(journey.electricalSafetyStep) }
                }
            }
            if (EPC in requiredStepTypes) {
                step(journey.epcStep) {
                    routeSegment(ReactivationEpcStep.ROUTE_SEGMENT)
                    parents { stepBefore(journey.epcStep).isComplete() }
                    nextStep { stepAfter(journey.epcStep) }
                }
            }
            if (TENANCY_DETAILS in requiredStepTypes) {
                step(journey.tenancyDetailsStep) {
                    routeSegment(ReactivationTenancyDetailsStep.ROUTE_SEGMENT)
                    parents { stepBefore(journey.tenancyDetailsStep).isComplete() }
                    nextStep { stepAfter(journey.tenancyDetailsStep) }
                }
            }
            step(journey.checkAnswersStep) {
                routeSegment(ReactivationCheckAnswersStep.ROUTE_SEGMENT)
                parents { stepBefore(journey.checkAnswersStep).isComplete() }
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
            state.requiredStepTypes = ReactivationStepType.requiredFor(propertyOwnership)
        }
        return state.initialiseFromPropertyOwnershipId(propertyOwnershipId)
    }

    fun initializeJourneyState(propertyOwnershipId: Long): String = stateFactory.getObject().initializeState(propertyOwnershipId)

    private fun PropertyReactivationJourneyState.getStep(stepType: ReactivationStepType) =
        when (stepType) {
            LICENSING -> licensingStep
            GAS_SAFETY -> gasSafetyStep
            ELECTRICAL_SAFETY -> electricalSafetyStep
            EPC -> epcStep
            TENANCY_DETAILS -> tenancyDetailsStep
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
    override var requiredStepTypes: List<ReactivationStepType> by delegateProvider.requiredImmutableDelegate("requiredStepTypes")

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
    var requiredStepTypes: List<ReactivationStepType>
}
