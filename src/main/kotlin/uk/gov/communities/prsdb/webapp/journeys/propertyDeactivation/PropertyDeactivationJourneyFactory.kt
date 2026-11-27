package uk.gov.communities.prsdb.webapp.journeys.propertyDeactivation

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyDeactivation.stepConfig.CanDeactivateMode
import uk.gov.communities.prsdb.webapp.journeys.propertyDeactivation.stepConfig.CannotDeactivateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeactivation.stepConfig.CheckCanDeactivateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeactivation.stepConfig.ConfirmDeactivationStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.initialiseFromPropertyOwnershipId
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.CheckPendingInvitationsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.HasPendingInvitationsMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.HasPendingInvitationsStep

@PrsdbWebService
class PropertyDeactivationJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyDeactivationJourney>,
) {
    fun createJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = getInitializedState(propertyOwnershipId)

        return journey(state) {
            unreachableStepStep { journey.checkCanDeactivateStep }
            step(journey.checkCanDeactivateStep) {
                routeSegment(CheckCanDeactivateStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) }
                nextStep { mode ->
                    when (mode) {
                        CanDeactivateMode.SINGLE_LANDLORD -> journey.hasPendingInvitationsStep
                        CanDeactivateMode.HAS_JOINT_LANDLORDS -> journey.cannotDeactivateStep
                    }
                }
            }
            step(journey.cannotDeactivateStep) {
                routeSegment(CannotDeactivateStep.ROUTE_SEGMENT)
                parents { journey.checkCanDeactivateStep.hasOutcome(CanDeactivateMode.HAS_JOINT_LANDLORDS) }
                noNextDestination()
            }
            step(journey.hasPendingInvitationsStep) {
                routeSegment(HasPendingInvitationsStep.ROUTE_SEGMENT)
                parents { journey.checkCanDeactivateStep.hasOutcome(CanDeactivateMode.SINGLE_LANDLORD) }
                nextStep { mode ->
                    when (mode) {
                        HasPendingInvitationsMode.YES -> journey.checkPendingInvitationsStep
                        HasPendingInvitationsMode.NO -> journey.confirmDeactivationStep
                    }
                }
            }
            step(journey.checkPendingInvitationsStep) {
                routeSegment(CheckPendingInvitationsStep.ROUTE_SEGMENT)
                withAdditionalContentProperty { "messagePrefix" to "deactivateProperty" }
                parents { journey.hasPendingInvitationsStep.hasOutcome(HasPendingInvitationsMode.YES) }
                nextStep { journey.confirmDeactivationStep }
            }
            step(journey.confirmDeactivationStep) {
                routeSegment(ConfirmDeactivationStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.hasPendingInvitationsStep.hasOutcome(HasPendingInvitationsMode.NO),
                        journey.checkPendingInvitationsStep.isComplete(),
                    )
                }
                nextDestination {
                    Destination
                        .ExternalUrl(PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId))
                        .withFlashAttribute("deactivationSuccessBanner", "propertyDetails.deactivationSuccessBanner.propertyDeactivated")
                }
            }
        }
    }

    private fun getInitializedState(propertyOwnershipId: Long): PropertyDeactivationJourney =
        stateFactory.getObject().initialiseFromPropertyOwnershipId(propertyOwnershipId)

    fun initializeJourneyState(propertyOwnershipId: Long): String = stateFactory.getObject().initializeState(propertyOwnershipId)
}

@JourneyFrameworkComponent
class PropertyDeactivationJourney(
    override val checkCanDeactivateStep: CheckCanDeactivateStep,
    override val cannotDeactivateStep: CannotDeactivateStep,
    override val hasPendingInvitationsStep: HasPendingInvitationsStep,
    override val checkPendingInvitationsStep: CheckPendingInvitationsStep,
    override val confirmDeactivationStep: ConfirmDeactivationStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    PropertyDeactivationJourneyState {
    override var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var propertyOwnershipId: Long by delegateProvider.requiredImmutableDelegate("propertyOwnershipId")

    override fun generateJourneyId(seed: Any?): String {
        val propertyOwnershipId = seed as? Long
        return super<AbstractJourneyState>.generateJourneyId(
            propertyOwnershipId?.let { generateSeedForPropertyOwnership(it) },
        )
    }

    companion object {
        private fun generateSeedForPropertyOwnership(propertyOwnershipId: Long): String =
            "Property deactivation journey for property $propertyOwnershipId at time ${System.currentTimeMillis()}"
    }
}

interface PropertyDeactivationJourneyState : PropertyOwnershipJourneyState {
    val checkCanDeactivateStep: CheckCanDeactivateStep
    val cannotDeactivateStep: CannotDeactivateStep
    val hasPendingInvitationsStep: HasPendingInvitationsStep
    val checkPendingInvitationsStep: CheckPendingInvitationsStep
    val confirmDeactivationStep: ConfirmDeactivationStep
}
