package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.AreYouSureMode
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.CanDeregisterMode
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.CannotDeregisterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.CheckCanDeregisterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.ConfirmStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.DeregisterInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.ReasonStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.initialiseFromPropertyOwnershipId
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.CheckPendingInvitationsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.HasPendingInvitationsMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.HasPendingInvitationsStep

@PrsdbWebService
class PropertyDeregistrationJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyDeregistrationJourney>,
) {
    fun createJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = getInitializedState(propertyOwnershipId)

        return journey(state) {
            unreachableStepStep { journey.checkCanDeregisterStep }
            configure {
                withAdditionalContentProperty { "title" to "deregisterProperty.title" }
            }
            step(journey.checkCanDeregisterStep) {
                routeSegment(CheckCanDeregisterStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) }
                nextStep { mode ->
                    when (mode) {
                        CanDeregisterMode.SINGLE_LANDLORD -> journey.deregisterInfoStep
                        CanDeregisterMode.HAS_JOINT_LANDLORDS -> journey.cannotDeregisterStep
                    }
                }
            }
            step(journey.cannotDeregisterStep) {
                routeSegment(CannotDeregisterStep.ROUTE_SEGMENT)
                parents { journey.checkCanDeregisterStep.hasOutcome(CanDeregisterMode.HAS_JOINT_LANDLORDS) }
                backUrl { PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) }
                noNextDestination()
            }
            step(journey.deregisterInfoStep) {
                routeSegment(DeregisterInfoStep.ROUTE_SEGMENT)
                parents { journey.checkCanDeregisterStep.hasOutcome(CanDeregisterMode.SINGLE_LANDLORD) }
                backUrl { PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) }
                nextDestination { Destination(journey.hasPendingInvitationsStep) }
            }
            step(journey.hasPendingInvitationsStep) {
                routeSegment(HasPendingInvitationsStep.ROUTE_SEGMENT)
                parents { journey.deregisterInfoStep.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        HasPendingInvitationsMode.YES -> journey.checkPendingInvitationsStep
                        HasPendingInvitationsMode.NO -> journey.confirmStep
                    }
                }
            }
            step(journey.checkPendingInvitationsStep) {
                routeSegment(CheckPendingInvitationsStep.ROUTE_SEGMENT)
                withAdditionalContentProperty { "messagePrefix" to "deregisterProperty" }
                parents { journey.hasPendingInvitationsStep.hasOutcome(HasPendingInvitationsMode.YES) }
                backUrl {
                    DeregisterPropertyController.getPropertyDeregistrationBasePath(propertyOwnershipId) +
                        "/${DeregisterInfoStep.ROUTE_SEGMENT}"
                }
                nextStep { journey.confirmStep }
            }
            step(journey.confirmStep) {
                routeSegment(ConfirmStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.hasPendingInvitationsStep.hasOutcome(HasPendingInvitationsMode.NO),
                        journey.checkPendingInvitationsStep.isComplete(),
                    )
                }
                nextUrl {
                    "${
                        DeregisterPropertyController.getPropertyDeregistrationBasePath(
                            propertyOwnershipId,
                        )
                    }/$CONFIRMATION_PATH_SEGMENT"
                }
            }
        }
    }

//    TODO PDJB-319: Remove this
    fun createOldJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = getInitializedState(propertyOwnershipId)

        return journey(state) {
            unreachableStepStep { journey.areYouSureStep }
            configure {
                withAdditionalContentProperty { "title" to "deregisterProperty.title" }
            }
            step(journey.areYouSureStep) {
                routeSegment(AreYouSureStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) }
                nextDestination { mode ->
                    if (mode == AreYouSureMode.DOES_NOT_WANT_TO_PROCEED) {
                        Destination.ExternalUrl(PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId))
                    } else {
                        Destination(journey.reasonStep)
                    }
                }
            }
            step(journey.reasonStep) {
                routeSegment(ReasonStep.ROUTE_SEGMENT)
                parents { journey.areYouSureStep.hasOutcome(AreYouSureMode.WANTS_TO_PROCEED) }
                nextUrl {
                    "${
                        DeregisterPropertyController.getPropertyDeregistrationBasePath(
                            propertyOwnershipId,
                        )
                    }/$CONFIRMATION_PATH_SEGMENT"
                }
            }
        }
    }

    private fun getInitializedState(propertyOwnershipId: Long): PropertyDeregistrationJourney =
        stateFactory.getObject().initialiseFromPropertyOwnershipId(propertyOwnershipId)

    fun initializeJourneyState(propertyOwnershipId: Long): String = stateFactory.getObject().initializeState(propertyOwnershipId)
}

@JourneyFrameworkComponent
class PropertyDeregistrationJourney(
    override val areYouSureStep: AreYouSureStep,
    override val checkCanDeregisterStep: CheckCanDeregisterStep,
    override val deregisterInfoStep: DeregisterInfoStep,
    override val cannotDeregisterStep: CannotDeregisterStep,
    override val hasPendingInvitationsStep: HasPendingInvitationsStep,
    override val checkPendingInvitationsStep: CheckPendingInvitationsStep,
    override val confirmStep: ConfirmStep,
    override val reasonStep: ReasonStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    PropertyDeregistrationJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
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
            "Property deregistration journey for property $propertyOwnershipId at time ${System.currentTimeMillis()}"
    }
}

interface PropertyDeregistrationJourneyState : PropertyOwnershipJourneyState {
    val areYouSureStep: AreYouSureStep
    val checkCanDeregisterStep: CheckCanDeregisterStep
    val deregisterInfoStep: DeregisterInfoStep
    val cannotDeregisterStep: CannotDeregisterStep
    val hasPendingInvitationsStep: HasPendingInvitationsStep
    val checkPendingInvitationsStep: CheckPendingInvitationsStep
    val confirmStep: ConfirmStep
    val reasonStep: ReasonStep
}
