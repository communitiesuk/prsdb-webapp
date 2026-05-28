package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.JOINT_LANDLORD_INVITATION_REJECTED_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_START_PAGE_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.AcceptOrRejectStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.CheckUserRoleStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.ConfirmYouAreALandlordForThisPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.InviteUnavailableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.TokenValidationResult
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.UserRoleStatus
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
            configure {
                withAdditionalContentProperty { "title" to "acceptOrRejectJointLandlordInvitation.title" }
            }
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
                        YesOrNo.YES -> {
                            Destination(journey.checkUserRoleStep).toUrlStringOrNull()
                                ?: throw PrsdbWebException("Url string for nextUrl cannot be null")
                        }

                        YesOrNo.NO -> {
                            JOINT_LANDLORD_INVITATION_REJECTED_CONFIRMATION_ROUTE
                        }
                    }
                }
            }
            // TODO PDJB-260 - direct back to here after ll registration journey?
            step(journey.checkUserRoleStep) {
                routeSegment(CheckUserRoleStep.ROUTE_SEGMENT)
                parents { journey.acceptOrRejectStep.hasOutcome(YesOrNo.YES) }
                nextUrl { mode ->
                    when (mode) {
                        UserRoleStatus.USER_NOT_REGISTERED_AS_LANDLORD -> {
                            LANDLORD_REGISTRATION_START_PAGE_ROUTE
                        }

                        UserRoleStatus.USER_IS_ALREADY_REGISTERED_AS_LANDLORD -> {
                            Destination(journey.confirmYouAreALandlordForThisPropertyStep).toUrlStringOrNull()
                                ?: throw PrsdbWebException("Url string for nextUrl cannot be null")
                        }
                    }
                }
            }
            step(journey.confirmYouAreALandlordForThisPropertyStep) {
                routeSegment(ConfirmYouAreALandlordForThisPropertyStep.ROUTE_SEGMENT)
                parents { journey.checkUserRoleStep.hasOutcome(UserRoleStatus.USER_IS_ALREADY_REGISTERED_AS_LANDLORD) }
                nextUrl { JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE }
            }
            step(journey.inviteUnavailableStep) {
                routeSegment(InviteUnavailableStep.ROUTE_SEGMENT)
                parents { journey.validateTokenStep.hasOutcome(TokenValidationResult.INVALID) }
                // TODO PDJB-266 - update routing once the inviteUnavailableStep is implemented
                nextUrl { JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE }
            }
        }
    }

    fun initializeJourneyState(token: String): String = stateFactory.getObject().initializeState(token)
}

@JourneyFrameworkComponent
class AcceptOrRejectJointLandlordInvitationJourney(
    override val validateTokenStep: ValidateTokenStep,
    override val acceptOrRejectStep: AcceptOrRejectStep,
    override val checkUserRoleStep: CheckUserRoleStep,
    override val confirmYouAreALandlordForThisPropertyStep: ConfirmYouAreALandlordForThisPropertyStep,
    override val inviteUnavailableStep: InviteUnavailableStep,
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
    val validateTokenStep: ValidateTokenStep
    val acceptOrRejectStep: AcceptOrRejectStep
    val checkUserRoleStep: CheckUserRoleStep
    val confirmYouAreALandlordForThisPropertyStep: ConfirmYouAreALandlordForThisPropertyStep
    val inviteUnavailableStep: InviteUnavailableStep
}
