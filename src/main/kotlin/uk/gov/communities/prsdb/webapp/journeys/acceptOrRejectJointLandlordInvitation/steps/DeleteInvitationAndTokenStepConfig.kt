package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationRejectionEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService

@JourneyFrameworkComponent
class DeleteInvitationAndTokenStepConfig(
    private val invitationService: JointLandlordInvitationService,
    private val invitationRepository: JointLandlordInvitationRepository,
    private val rejectionEmailSender: EmailNotificationService<JointLandlordInvitationRejectionEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) : AbstractInternalStepConfig<Complete, AcceptOrRejectJointLandlordInvitationJourneyState>() {
    override fun mode(state: AcceptOrRejectJointLandlordInvitationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: AcceptOrRejectJointLandlordInvitationJourneyState) {
        val invitation = invitationService.getInvitationForJourney(state.journeyId)

        if (state.acceptOrRejectStep.outcome == YesOrNo.NO) {
            val propertyAddress = invitation.registeredOwnership.address.singleLineAddress
            val propertyRecordUrl =
                absoluteUrlProvider.buildPropertyDetailsUri(invitation.registeredOwnership.id).toString()

            // Store data for the rejection confirmation page
            invitationService.addRejectionConfirmationDataToSession(propertyAddress)

            // Send rejection email to all landlords on the property
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

        invitationRepository.delete(invitation)

        val token = invitationService.getInvitationTokenForJourneyIdFromSession(state.journeyId)
        invitationService.clearJourneyIdInvitationTokenPairsForTokenFromSession(token)
    }

    override fun resolveNextDestination(
        state: AcceptOrRejectJointLandlordInvitationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class DeleteInvitationAndTokenStep(
    stepConfig: DeleteInvitationAndTokenStepConfig,
) : JourneyStep.InternalStep<Complete, AcceptOrRejectJointLandlordInvitationJourneyState>(stepConfig)
