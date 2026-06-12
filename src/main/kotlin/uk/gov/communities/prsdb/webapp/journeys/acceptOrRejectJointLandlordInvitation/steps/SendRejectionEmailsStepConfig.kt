package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationRejectionEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService

@JourneyFrameworkComponent
class SendRejectionEmailsStepConfig(
    private val invitationService: JointLandlordInvitationService,
    private val rejectionEmailSender: EmailNotificationService<JointLandlordInvitationRejectionEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) : AbstractInternalStepConfig<Complete, AcceptOrRejectJointLandlordInvitationJourneyState>() {
    override fun mode(state: AcceptOrRejectJointLandlordInvitationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: AcceptOrRejectJointLandlordInvitationJourneyState) {
        val invitation = invitationService.getInvitationForJourney(state.journeyId)
        val propertyAddress = invitation.registeredOwnership.address.singleLineAddress
        val propertyRecordUrl =
            absoluteUrlProvider.buildPropertyDetailsUri(invitation.registeredOwnership.id).toString()

        invitationService.addRejectedPropertyAddressToSession(propertyAddress)

        invitation.registeredOwnership.landlords.forEach { landlord ->
            rejectionEmailSender.sendEmail(
                landlord.email,
                JointLandlordInvitationRejectionEmail(
                    recipientName = landlord.name,
                    inviteeEmail = invitation.invitedEmail,
                    propertyAddress = propertyAddress,
                    propertyRecordUrl = propertyRecordUrl,
                ),
            )
        }
    }
}

@JourneyFrameworkComponent
class SendRejectionEmailsStep(
    stepConfig: SendRejectionEmailsStepConfig,
) : JourneyStep.InternalStep<Complete, AcceptOrRejectJointLandlordInvitationJourneyState>(stepConfig)
