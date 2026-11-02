package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.JOINT_LANDLORD_INVITATION_REJECTED_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.AcceptOrRejectStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.CheckUserRoleStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.ConfirmYouAreALandlordForThisPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.DeleteInvitationAndTokenStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.InviteUnavailableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.MarkLandlordRegistrationCompleteStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.SendRejectionEmailsStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.TokenValidationResult
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.UserRoleStatus
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.ValidateTokenStep
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LandlordRegistrationTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class AcceptOrRejectJointLandlordInvitationJourneyFactory(
    private val stateFactory: ObjectFactory<AcceptOrRejectJointLandlordInvitationJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        val checkingAnswersFor = state.checkingAnswersFor

        return if (checkingAnswersFor == null) {
            mainJourneyMap(state)
        } else {
            LandlordRegistrationTask.checkYourAnswersJourneyMap(state.landlordRegistrationTask, checkingAnswersFor)
        }
    }

    private fun mainJourneyMap(state: AcceptOrRejectJointLandlordInvitationJourneyState): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
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
                nextDestination { mode ->
                    when (mode) {
                        YesOrNo.YES -> Destination(journey.checkUserRoleStep)
                        YesOrNo.NO -> Destination(journey.sendRejectionEmailsStep)
                    }
                }
            }
            step(journey.checkUserRoleStep) {
                routeSegment(CheckUserRoleStep.ROUTE_SEGMENT)
                parents { journey.acceptOrRejectStep.hasOutcome(YesOrNo.YES) }
                nextDestination { mode ->
                    when (mode) {
                        UserRoleStatus.USER_NOT_REGISTERED_AS_LANDLORD -> Destination(journey.landlordRegistrationTask.firstStep)

                        UserRoleStatus.USER_IS_ALREADY_REGISTERED_AS_LANDLORD -> {
                            Destination(journey.confirmYouAreALandlordForThisPropertyStep)
                        }

                        UserRoleStatus.USER_IS_ALREADY_LANDLORD_ON_PROPERTY -> {
                            val propertyOwnershipId =
                                journey.propertyOwnershipIdUserIsAlreadyLandlordOn
                                    ?: throw NotNullFormModelValueIsNullException(
                                        "propertyOwnershipIdUserIsAlreadyLandlordOn is null when user is already landlord on property",
                                    )
                            Destination.ExternalUrl(PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId))
                        }
                    }
                }
            }
            task(journey.landlordRegistrationTask) {
                parents { journey.acceptOrRejectStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.markLandlordRegistrationCompleteStep }
            }
            step(journey.markLandlordRegistrationCompleteStep) {
                parents { journey.acceptOrRejectStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.checkUserRoleStep }
            }
            step(journey.confirmYouAreALandlordForThisPropertyStep) {
                routeSegment(ConfirmYouAreALandlordForThisPropertyStep.ROUTE_SEGMENT)
                parents { journey.checkUserRoleStep.hasOutcome(UserRoleStatus.USER_IS_ALREADY_REGISTERED_AS_LANDLORD) }
                nextStep {
                    when (state.tokenIsValid) {
                        true -> journey.deleteInvitationAndTokenStep

                        false -> journey.inviteUnavailableStep

                        null -> throw NotNullFormModelValueIsNullException(
                            "tokenIsValid is null when trying to determine next step after confirmYouAreALandlordForThisPropertyStep",
                        )
                    }
                }
            }
            step(journey.sendRejectionEmailsStep) {
                parents { journey.acceptOrRejectStep.hasOutcome(YesOrNo.NO) }
                nextStep { journey.deleteInvitationAndTokenStep }
            }
            step(journey.deleteInvitationAndTokenStep) {
                parents {
                    OrParents(
                        journey.confirmYouAreALandlordForThisPropertyStep.isComplete(),
                        journey.sendRejectionEmailsStep.isComplete(),
                    )
                }
                nextUrl {
                    when (state.acceptOrRejectStep.outcome) {
                        YesOrNo.YES -> JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE

                        YesOrNo.NO -> JOINT_LANDLORD_INVITATION_REJECTED_CONFIRMATION_ROUTE

                        null -> throw NotNullFormModelValueIsNullException(
                            "Accept or reject step outcome is null when trying to determine next URL in clean up and redirect step",
                        )
                    }
                }
            }
            step(journey.inviteUnavailableStep) {
                routeSegment(InviteUnavailableStep.ROUTE_SEGMENT)
                parents { journey.validateTokenStep.hasOutcome(TokenValidationResult.INVALID) }
                noNextDestination()
            }
        }

    fun initializeJourneyState(token: String): String = stateFactory.getObject().initializeState(token)
}

@JourneyFrameworkComponent("acceptOrRejectJointLandlordInvitationJourney")
class AcceptOrRejectJointLandlordInvitationJourney(
    override val validateTokenStep: ValidateTokenStep,
    override val acceptOrRejectStep: AcceptOrRejectStep,
    override val checkUserRoleStep: CheckUserRoleStep,
    override val markLandlordRegistrationCompleteStep: MarkLandlordRegistrationCompleteStep,
    override val confirmYouAreALandlordForThisPropertyStep: ConfirmYouAreALandlordForThisPropertyStep,
    override val sendRejectionEmailsStep: SendRejectionEmailsStep,
    override val deleteInvitationAndTokenStep: DeleteInvitationAndTokenStep,
    override val inviteUnavailableStep: InviteUnavailableStep,
    // Landlord registration task
    override val landlordRegistrationTask: LandlordRegistrationTask,
    private val invitationService: JointLandlordInvitationService,
    private val propertyOwnershipService: PropertyOwnershipService,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    AcceptOrRejectJointLandlordInvitationJourneyState {
    override val checkingAnswersFor: String? get() = landlordRegistrationTask.checkingAnswersFor

    override var tokenIsValid: Boolean? by delegateProvider.nullableDelegate("tokenIsValid")
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)

    override var userIsLandlord: Boolean? by delegateProvider.nullableDelegate("userIsLandlord")
    override var propertyOwnershipIdUserIsAlreadyLandlordOn: Long? by delegateProvider.nullableDelegate(
        "propertyOwnershipIdUserIsAlreadyLandlordOn",
    )
    override var userCompletedLandlordRegistrationThisJourney: Boolean? by delegateProvider.nullableDelegate(
        "userCompletedLandlordRegistrationThisJourney",
    )
    override var registeredLandlordRegistrationNumber: String? by delegateProvider.nullableDelegate(
        "registeredLandlordRegistrationNumber",
    )

    override fun getInvitedPropertyOwnershipIdIfCurrentUserIsLandlordOnIt(): Long? {
        val token = invitationService.getInvitationTokenForJourneyIdFromSession(journeyId)
        if (!invitationService.getTokenIsValid(token)) return null

        val propertyOwnershipId = invitationService.getInvitationFromToken(token).registeredOwnership.id
        return propertyOwnershipId.takeIf { propertyOwnershipService.isCurrentUserLandlord(it) }
    }

    override fun isCurrentUserLandlordOnInvitedPropertyOwnership(): Boolean =
        getInvitedPropertyOwnershipIdIfCurrentUserIsLandlordOnIt() != null

    override fun generateJourneyId(seed: Any?): String {
        val token = seed as? String
        return super<AbstractJourneyState>.generateJourneyId(
            token?.let { "Accept or reject joint landlord invitation journey for token $it at time ${System.currentTimeMillis()}" },
        )
    }
}

interface AcceptOrRejectJointLandlordInvitationJourneyState : JourneyState {
    val landlordRegistrationTask: LandlordRegistrationTask
    val checkingAnswersFor: String?

    val validateTokenStep: ValidateTokenStep
    val acceptOrRejectStep: AcceptOrRejectStep
    val checkUserRoleStep: CheckUserRoleStep
    val markLandlordRegistrationCompleteStep: MarkLandlordRegistrationCompleteStep
    val confirmYouAreALandlordForThisPropertyStep: ConfirmYouAreALandlordForThisPropertyStep
    val sendRejectionEmailsStep: SendRejectionEmailsStep
    val deleteInvitationAndTokenStep: DeleteInvitationAndTokenStep
    val inviteUnavailableStep: InviteUnavailableStep

    var tokenIsValid: Boolean?
    var userIsLandlord: Boolean?
    var propertyOwnershipIdUserIsAlreadyLandlordOn: Long?
    var userCompletedLandlordRegistrationThisJourney: Boolean?
    var registeredLandlordRegistrationNumber: String?

    fun getInvitedPropertyOwnershipIdIfCurrentUserIsLandlordOnIt(): Long?

    fun isCurrentUserLandlordOnInvitedPropertyOwnership(): Boolean
}
