package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.AreYouSureMode
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.DeregistrationCheckInvitationsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.ReasonStep
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class PropertyDeregistrationJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyDeregistrationJourney>,
    private val featureFlagManager: FeatureFlagManager,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
) {
    fun createJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyOwnershipId = propertyOwnershipId
            state.isStateInitialized = true
        }

        if (state.propertyOwnershipId != propertyOwnershipId) {
            throw PropertyOwnershipMismatchException(
                "Journey was initialized for property ownership ${state.propertyOwnershipId} " +
                    "but request is for property ownership $propertyOwnershipId",
            )
        }

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
                    if (!featureFlagManager.checkFeature(JOINT_LANDLORDS) && mode == AreYouSureMode.DOES_NOT_WANT_TO_PROCEED) {
                        Destination.ExternalUrl(PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId))
                    } else if (featureFlagManager.checkFeature(JOINT_LANDLORDS) && hasPendingInvitations(propertyOwnershipId)) {
                        Destination(journey.checkInvitationsStep)
                    } else {
                        Destination(journey.reasonStep)
                    }
                }
            }
            step(journey.checkInvitationsStep) {
                routeSegment(DeregistrationCheckInvitationsStep.ROUTE_SEGMENT)
                parents { journey.areYouSureStep.hasOutcome(AreYouSureMode.WANTS_TO_PROCEED) }
                backUrl {
                    DeregisterPropertyController.getPropertyDeregistrationBasePath(propertyOwnershipId) +
                        "/${AreYouSureStep.ROUTE_SEGMENT}"
                }
                nextDestination { Destination(journey.reasonStep) }
            }
            step(journey.reasonStep) {
                routeSegment(ReasonStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.areYouSureStep.hasOutcome(AreYouSureMode.WANTS_TO_PROCEED),
                        journey.checkInvitationsStep.isComplete(),
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

    private fun hasPendingInvitations(propertyOwnershipId: Long): Boolean {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)
        val (pendingInvitations, _) = jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership)
        return pendingInvitations.isNotEmpty()
    }

    fun initializeJourneyState(propertyOwnershipId: Long): String = stateFactory.getObject().initializeState(propertyOwnershipId)
}

@JourneyFrameworkComponent
class PropertyDeregistrationJourney(
    // Are you sure step
    override val areYouSureStep: AreYouSureStep,
    // Check invitations step
    override val checkInvitationsStep: DeregistrationCheckInvitationsStep,
    // Reason step
    override val reasonStep: ReasonStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    PropertyDeregistrationJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
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

interface PropertyDeregistrationJourneyState : JourneyState {
    val areYouSureStep: AreYouSureStep
    val checkInvitationsStep: DeregistrationCheckInvitationsStep
    val reasonStep: ReasonStep
    var propertyOwnershipId: Long
}
