package uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_FRAGMENT
import uk.gov.communities.prsdb.webapp.controllers.CancelJointLandlordInvitationController.Companion.CANCEL_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.stepConfig.AreYouSureMode
import uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.stepConfig.CancelInvitationStep
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService

@PrsdbWebService
class CancelJointLandlordInvitationJourneyFactory(
    private val stateFactory: ObjectFactory<CancelJointLandlordInvitationJourney>,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
) {
    fun createJourneySteps(
        invitationId: Long,
        baseUserId: String,
    ): Map<String, StepLifecycleOrchestrator> {
        val invitation = jointLandlordInvitationService.getPendingInvitationIfAuthorizedLandlord(invitationId, baseUserId)
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.invitedEmail = invitation.invitedEmail
            state.invitationId = invitation.id
            state.propertyOwnershipId = invitation.registeredOwnership.id
            state.isStateInitialized = true
        }

        val propertyRecordUrl =
            PropertyDetailsController.getPropertyDetailsPath(invitation.registeredOwnership.id) +
                "#$LANDLORD_DETAILS_FRAGMENT"

        return journey(state) {
            unreachableStepStep { journey.areYouSureStep }
            configure {
                withAdditionalContentProperty { "title" to "cancelJointLandlordInvitation.title" }
            }
            step(journey.areYouSureStep) {
                routeSegment(AreYouSureStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { propertyRecordUrl }
                nextDestination { mode ->
                    if (mode == AreYouSureMode.DOES_NOT_WANT_TO_PROCEED) {
                        Destination.ExternalUrl(propertyRecordUrl)
                    } else {
                        Destination(journey.cancelInvitationStep)
                    }
                }
            }
            step(journey.cancelInvitationStep) {
                parents { journey.areYouSureStep.hasOutcome(AreYouSureMode.WANTS_TO_PROCEED) }
                nextUrl {
                    "$CANCEL_JOINT_LANDLORD_INVITATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" +
                        "?propertyOwnershipId=${state.propertyOwnershipId}"
                }
            }
        }
    }

    fun initializeJourneyState(): String = stateFactory.getObject().initializeState()
}

@JourneyFrameworkComponent
class CancelJointLandlordInvitationJourney(
    override val areYouSureStep: AreYouSureStep,
    override val cancelInvitationStep: CancelInvitationStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    CancelJointLandlordInvitationJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var invitedEmail: String by delegateProvider.requiredDelegate("invitedEmail")
    override var invitationId: Long by delegateProvider.requiredDelegate("invitationId")
    override var propertyOwnershipId: Long by delegateProvider.requiredDelegate("propertyOwnershipId")

    override fun generateJourneyId(seed: Any?): String =
        super<AbstractJourneyState>.generateJourneyId(
            "Cancel joint landlord invitation journey at time ${System.currentTimeMillis()}",
        )
}

interface CancelJointLandlordInvitationJourneyState : JourneyState {
    val areYouSureStep: AreYouSureStep
    val cancelInvitationStep: CancelInvitationStep
    var invitedEmail: String
    var invitationId: Long
    var propertyOwnershipId: Long
}
