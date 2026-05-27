package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.JOINT_LANDLORD_INVITATION_REJECTED_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.AcceptOrRejectStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.InviteUnavailableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.TokenValidationResult
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.ValidateTokenStep
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@PrsdbWebService
class AcceptOrRejectJointLandlordInvitationJourneyFactory(
    private val stateFactory: ObjectFactory<AcceptOrRejectJointLandlordInvitationJourney>,
) {
    fun createJourneySteps(token: String): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.invitationToken = token
            state.isStateInitialized = true
        }

        if (state.invitationToken != token) {
            throw PrsdbWebException(
                "Journey state token ${state.invitationToken} does not match provided token $token",
            )
        }

        return journey(state) {
            unreachableStepStep { journey.validateTokenStep }
            step(journey.validateTokenStep) {
                routeSegment(ValidateTokenStep.ROUTE_SEGMENT)
                initialStep()
                nextStep { mode ->
                    when (mode) {
                        TokenValidationResult.VALID -> journey.acceptOrRejectStep
                        TokenValidationResult.INVALID -> journey.inviteUnavailableStep
                    }
                }
            }
            step(journey.acceptOrRejectStep) {
                routeSegment(AcceptOrRejectStep.ROUTE_SEGMENT)
                parents { journey.validateTokenStep.hasOutcome(TokenValidationResult.VALID) }
                nextUrl { mode ->
                    when (mode) {
                        // TODO PDJB-260 - if they submit yes we will need to check if the user is a registered latndlord and if not they need to register
                        //   The existing landlord registration journey covers most of this but can't be used as-is as we need different exit points here
                        YesOrNo.YES -> JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE

                        YesOrNo.NO -> JOINT_LANDLORD_INVITATION_REJECTED_CONFIRMATION_ROUTE
                    }
                }
            }
            step(journey.inviteUnavailableStep) {
                routeSegment(InviteUnavailableStep.ROUTE_SEGMENT)
                parents { journey.validateTokenStep.hasOutcome(TokenValidationResult.INVALID) }
                nextUrl { JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE }
            }
        }
    }

    fun initializeJourneyState(token: String): String = stateFactory.getObject().initializeState()
}

@JourneyFrameworkComponent
class AcceptOrRejectJointLandlordInvitationJourney(
    val validateTokenStep: ValidateTokenStep,
    val acceptOrRejectStep: AcceptOrRejectStep,
    val inviteUnavailableStep: InviteUnavailableStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    AcceptOrRejectJointLandlordInvitationJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var invitationToken: String by delegateProvider.requiredImmutableDelegate("invitationToken")
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)

    override fun generateJourneyId(seed: Any?): String {
        val token = seed as? String
        return super<AbstractJourneyState>.generateJourneyId(
            token?.let { "Accept or reject joint landlord invitation journey for token $it" },
        )
    }
}

interface AcceptOrRejectJointLandlordInvitationJourneyState : JourneyState {
    val invitationToken: String
}
