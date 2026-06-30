package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.JointLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationExpiryEmail

@PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlord-invitation-expiry-email-flag-on")
interface JointLandlordInvitationExpiryEmailService {
    fun sendExpiryEmailsForExpiredInvitations(): List<Long>
}

@Primary
@PrsdbTaskService("joint-landlord-invitation-expiry-email-flag-off")
class JointLandlordInvitationExpiryEmailServiceImplFlagOff : JointLandlordInvitationExpiryEmailService {
    override fun sendExpiryEmailsForExpiredInvitations(): List<Long> {
        // No-op: the joint-landlords feature is disabled, so we do not send expiry emails.
        return emptyList()
    }
}

@PrsdbTaskService("joint-landlord-invitation-expiry-email-flag-on")
class JointLandlordInvitationExpiryEmailServiceImplFlagOn(
    private val invitationRepository: JointLandlordInvitationRepository,
    private val expiryEmailNotificationService: EmailNotificationService<JointLandlordInvitationExpiryEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val swapToIndividualNudgeEmailService: SwapToIndividualNudgeEmailService,
) : JointLandlordInvitationExpiryEmailService {
    override fun sendExpiryEmailsForExpiredInvitations(): List<Long> {
        val expiredInvitations =
            invitationRepository
                .findAllByInvitationExpiredEmailSentFalse()
                .filter { it.status == JointLandlordInvitationStatus.EXPIRED }
        val expiredIds = mutableListOf<Long>()

        expiredInvitations.forEach { invitation ->
            try {
                sendExpiryEmailsForInvitation(invitation)
                invitation.markAsExpiredEmailSent()
                invitationRepository.save(invitation)
                expiredIds.add(invitation.id)
                swapToIndividualNudgeEmailService.sendNudgeEmailIfApplicable(invitation.registeredOwnership)
            } catch (ex: PersistentEmailSendException) {
                printFailureMessage(ex, invitation)
            } catch (ex: TransientEmailSentException) {
                printFailureMessage(ex, invitation)
            }
        }

        return expiredIds
    }

    private fun sendExpiryEmailsForInvitation(invitation: JointLandlordInvitation) {
        val propertyOwnership = invitation.registeredOwnership
        val propertyAddress = propertyOwnership.address.toMultiLineAddress()
        val propertyRecordUri = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id)

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
