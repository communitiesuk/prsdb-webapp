package uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.CancelJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationCancellationCancellerEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationCancellationInviteeEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationCancellationOtherLandlordEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.SwapToIndividualNudgeEmailService

@JourneyFrameworkComponent
class CancelInvitationStepConfig(
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val swapToIndividualNudgeEmailService: SwapToIndividualNudgeEmailService,
    private val landlordService: LandlordService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val inviteeEmailSender: EmailNotificationService<JointLandlordInvitationCancellationInviteeEmail>,
    private val cancellerEmailSender: EmailNotificationService<JointLandlordInvitationCancellationCancellerEmail>,
    private val otherLandlordEmailSender: EmailNotificationService<JointLandlordInvitationCancellationOtherLandlordEmail>,
) : AbstractInternalStepConfig<Complete, CancelJointLandlordInvitationJourneyState>() {
    override fun mode(state: CancelJointLandlordInvitationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: CancelJointLandlordInvitationJourneyState) {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        val invitation = jointLandlordInvitationService.getPendingInvitationIfAuthorizedLandlord(state.invitationId, baseUserId)
        val propertyOwnership = invitation.registeredOwnership
        val propertyAddress = propertyOwnership.address.toMultiLineAddress()
        val propertyRecordUrl =
            absoluteUrlProvider.buildPropertyDetailsUri(state.propertyOwnershipId).toString()
        val cancellerLandlord = landlordService.retrieveLandlordByBaseUserId(baseUserId)!!

        // Cancel the invitation
        jointLandlordInvitationService.removeInvitation(invitation)
        swapToIndividualNudgeEmailService.sendNudgeEmailIfApplicable(propertyOwnership)

        // Store cancelled email in session for confirmation page
        jointLandlordInvitationService.addOrUpdateCancelledInvitationEmailInSession(state.invitedEmail)

        // Email the invitee
        inviteeEmailSender.sendEmail(
            state.invitedEmail,
            JointLandlordInvitationCancellationInviteeEmail(propertyAddress = propertyAddress),
        )

        // Email the canceller
        cancellerEmailSender.sendEmail(
            cancellerLandlord.email,
            JointLandlordInvitationCancellationCancellerEmail(
                recipientName = cancellerLandlord.name,
                invitedEmail = state.invitedEmail,
                propertyAddress = propertyAddress,
                propertyRecordUrl = propertyRecordUrl,
            ),
        )

        // Email the other landlords
        propertyOwnership.landlords
            .filterNot { cancellerLandlord.id == it.id }
            .forEach {
                otherLandlordEmailSender.sendEmail(
                    it.email,
                    JointLandlordInvitationCancellationOtherLandlordEmail(
                        recipientName = it.name,
                        invitedEmail = state.invitedEmail,
                        propertyAddress = propertyAddress,
                        propertyRecordUrl = propertyRecordUrl,
                    ),
                )
            }
    }

    override fun resolveNextDestination(
        state: CancelJointLandlordInvitationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CancelInvitationStep(
    stepConfig: CancelInvitationStepConfig,
) : JourneyStep.InternalStep<Complete, CancelJointLandlordInvitationJourneyState>(stepConfig)
