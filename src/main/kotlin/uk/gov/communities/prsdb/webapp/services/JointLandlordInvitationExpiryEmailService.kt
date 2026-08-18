package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.JointLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationExpiryEmail

data class JointLandlordInvitationExpiryEmailResult(
    val sentIds: List<Long>,
    val failedIds: List<Long>,
)

@PrsdbTaskService
class JointLandlordInvitationExpiryEmailService(
    private val invitationRepository: JointLandlordInvitationRepository,
    private val expiryEmailNotificationService: EmailNotificationService<JointLandlordInvitationExpiryEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val swapToIndividualNudgeEmailService: SwapToIndividualNudgeEmailService,
) {
    fun sendExpiryEmailsForExpiredInvitations(): JointLandlordInvitationExpiryEmailResult {
        val expiredInvitations =
            invitationRepository
                .findAllByInvitationExpiredEmailSentFalse()
                .filter { it.status == JointLandlordInvitationStatus.EXPIRED }
        val sentIds = mutableListOf<Long>()
        val failedIds = mutableListOf<Long>()

        expiredInvitations.forEach { invitation ->
            try {
                sendExpiryEmailsForInvitation(invitation)
                invitation.markAsExpiredEmailSent()
                invitationRepository.save(invitation)
                sentIds.add(invitation.id)
                swapToIndividualNudgeEmailService.sendNudgeEmailIfApplicable(invitation.registeredOwnership)
            } catch (ex: PersistentEmailSendException) {
                printFailureMessage(ex, invitation)
                failedIds.add(invitation.id)
            } catch (ex: TransientEmailSentException) {
                printFailureMessage(ex, invitation)
                failedIds.add(invitation.id)
            }
        }

        return JointLandlordInvitationExpiryEmailResult(sentIds, failedIds)
    }

    private fun sendExpiryEmailsForInvitation(invitation: JointLandlordInvitation) {
        val propertyOwnership = invitation.registeredOwnership
        val propertyAddress = propertyOwnership.address.toMultiLineAddress()
        val propertyRecordUri = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id)

        // TODO: PDJB-1274: Update emails to account for org landlord
        propertyOwnership.landlords.forEach { recipient ->
            expiryEmailNotificationService.sendEmail(
                recipient.email,
                JointLandlordInvitationExpiryEmail(
                    recipientName = recipient.name,
                    invitedEmail = invitation.invitedEmail,
                    propertyAddress = propertyAddress,
                    propertyRecordUri = propertyRecordUri,
                    expiryDays = JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS,
                ),
            )
        }
    }

    private fun printFailureMessage(
        ex: Exception,
        invitation: JointLandlordInvitation,
    ) {
        println("Failed to send expiry email for joint landlord invitation with id: ${invitation.id}")
        println("Exception message: ${ex.message}")
        println("Stack trace: ${ex.stackTraceToString()}")
    }
}
