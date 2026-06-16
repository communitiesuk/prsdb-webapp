package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.JointLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationExpiryEmail

@PrsdbTaskService
class JointLandlordInvitationExpiryEmailService(
    private val invitationRepository: JointLandlordInvitationRepository,
    private val expiryEmailNotificationService: EmailNotificationService<JointLandlordInvitationExpiryEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val featureFlagManager: FeatureFlagManager,
) {
    fun sendExpiryEmailsForExpiredInvitations(): List<Long> {
        if (!featureFlagManager.checkFeature(JOINT_LANDLORDS)) {
            return emptyList()
        }

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
