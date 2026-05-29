package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_HOURS
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationExpiryEmail
import java.time.Instant
import java.time.temporal.ChronoUnit

@PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlord-invitation-expiry-flag-on")
interface JointLandlordInvitationExpiryService {
    fun expirePendingInvitations()
}

@Primary
@PrsdbWebService("joint-landlord-invitation-expiry-flag-off")
class JointLandlordInvitationExpiryServiceImplFlagOff : JointLandlordInvitationExpiryService {
    override fun expirePendingInvitations() {
        // No-op: the joint-landlords feature is disabled, so we do not expire invitations.
    }
}

@PrsdbWebService("joint-landlord-invitation-expiry-flag-on")
class JointLandlordInvitationExpiryServiceImplFlagOn(
    private val invitationRepository: JointLandlordInvitationRepository,
    private val expiryEmailNotificationService: EmailNotificationService<JointLandlordInvitationExpiryEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) : JointLandlordInvitationExpiryService {
    override fun expirePendingInvitations() {
        val cutoff = Instant.now().minus(JOINT_LANDLORD_INVITATION_LIFETIME_IN_HOURS.toLong(), ChronoUnit.HOURS)
        val expiredInvitations = invitationRepository.findAllByCreatedDateBefore(cutoff)

        expiredInvitations.forEach { invitation ->
            try {
                sendExpiryEmailsForInvitation(invitation)
                invitationRepository.delete(invitation)
            } catch (ex: PersistentEmailSendException) {
                printFailureMessage(ex, invitation)
            } catch (ex: TransientEmailSentException) {
                printFailureMessage(ex, invitation)
            }
        }
    }

    private fun sendExpiryEmailsForInvitation(invitation: JointLandlordInvitation) {
        val propertyOwnership = invitation.registeredOwnership
        val propertyAddress = propertyOwnership.address.toMultiLineAddress()
        val propertyRecordUri = absoluteUrlProvider.buildLandlordPropertyDetailsUri(propertyOwnership.id)

        getExpiryEmailRecipients(propertyOwnership).forEach { recipient ->
            expiryEmailNotificationService.sendEmail(
                recipient.email,
                JointLandlordInvitationExpiryEmail(
                    recipientName = recipient.name,
                    invitedEmail = invitation.invitedEmail,
                    propertyAddress = propertyAddress,
                    propertyRecordUri = propertyRecordUri,
                ),
            )
        }
    }

    // TODO PDJB-260: include accepted joint landlords once that data model exists.
    private fun getExpiryEmailRecipients(propertyOwnership: PropertyOwnership): List<Landlord> = listOf(propertyOwnership.primaryLandlord)

    private fun printFailureMessage(
        ex: Exception,
        invitation: JointLandlordInvitation,
    ) {
        println("Failed to send expiry email for joint landlord invitation with id: ${invitation.id}")
        println("Exception message: ${ex.message}")
        println("Stack trace: ${ex.stackTraceToString()}")
    }
}
