package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.ACCEPTED_JOINT_LANDLORD_PROPERTY_DETAILS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_EMAIL_CANCELLED
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_REJECTION_PROPERTY_ADDRESS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.enums.JointLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordInvitationNotifyExistingEmail
import java.util.UUID

@PrsdbWebService
class JointLandlordInvitationService(
    val invitationRepository: JointLandlordInvitationRepository,
    private val invitationEmailSender: EmailNotificationService<JointLandlordInvitationEmail>,
    private val confirmationEmailSender: EmailNotificationService<JointLandlordInvitationConfirmationEmail>,
    private val notifyExistingEmailSender: EmailNotificationService<JointLandlordInvitationNotifyExistingEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val session: HttpSession,
) {
    fun getPendingInvitations(propertyOwnership: PropertyOwnership): List<JointLandlordInvitation> =
        invitationRepository
            .findByRegisteredOwnership(propertyOwnership)
            .filter { it.status == JointLandlordInvitationStatus.PENDING }

    fun getPendingAndExpiredInvitations(
        propertyOwnership: PropertyOwnership,
    ): Pair<List<JointLandlordInvitation>, List<JointLandlordInvitation>> {
        val grouped =
            invitationRepository
                .findByRegisteredOwnership(propertyOwnership)
                .sortedByDescending { it.createdDate }
                .groupBy { it.status }
        val pending = grouped[JointLandlordInvitationStatus.PENDING].orEmpty()
        val expired = grouped[JointLandlordInvitationStatus.EXPIRED].orEmpty()
        return Pair(pending, expired)
    }

    fun getExistingInvitedEmails(ownershipId: Long): List<String> =
        invitationRepository
            .findByRegisteredOwnershipId(ownershipId)
            .filter { it.status != JointLandlordInvitationStatus.HIDDEN }
            .map { it.invitedEmail }

    fun sendInvitationEmails(
        jointLandlordEmails: List<String>,
        propertyOwnership: PropertyOwnership,
        invitingLandlord: Landlord,
    ) {
        val senderName = invitingLandlord.name
        val propertyAddress = propertyOwnership.address.toMultiLineAddress()

        // Re-check against the current state of the database when finishing the journey. The form-level checks happen
        // when an email is submitted, so without this a concurrent journey could invite the same email twice.
        val alreadyInvitedEmails = getExistingInvitedEmails(propertyOwnership.id)
        val existingLandlordEmails = propertyOwnership.landlords.map { it.email }
        val emailsToInvite =
            jointLandlordEmails.filter { it !in alreadyInvitedEmails && it !in existingLandlordEmails }

        emailsToInvite.forEach { email ->
            val token = UUID.randomUUID()
            val invitationUri = absoluteUrlProvider.buildJointLandlordInvitationUri(token.toString())

            // Save the invitation before sending the email so the link in the email always resolves to a real token.
            // If the email fails to send, delete the invitation again so we don't leave an orphaned record behind.
            val invitation = JointLandlordInvitation(token, email, propertyOwnership, invitingLandlord.name)
            invitationRepository.save(invitation)

            try {
                invitationEmailSender.sendEmail(
                    email,
                    JointLandlordInvitationEmail(
                        senderName = senderName,
                        propertyAddress = propertyAddress,
                        invitationUri = invitationUri,
                    ),
                )
            } catch (exception: Exception) {
                invitationRepository.delete(invitation)
                throw exception
            }
        }

        if (emailsToInvite.isNotEmpty()) {
            val propertyRecordUrl = absoluteUrlProvider.buildPropertyDetailsUri(propertyOwnership.id).toString()
            confirmationEmailSender.sendEmail(
                invitingLandlord.email,
                JointLandlordInvitationConfirmationEmail(
                    senderName = senderName,
                    propertyAddress = propertyAddress,
                    jointLandlordEmails = emailsToInvite,
                    propertyRecordUrl = propertyRecordUrl,
                ),
            )

            val existingJointLandlords = propertyOwnership.landlords.filter { it.id != invitingLandlord.id }
            existingJointLandlords.forEach { landlord ->
                notifyExistingEmailSender.sendEmail(
                    landlord.email,
                    JointLandlordInvitationNotifyExistingEmail(
                        recipientName = landlord.name,
                        propertyAddress = propertyAddress,
                        jointLandlordEmails = emailsToInvite,
                        propertyRecordUrl = propertyRecordUrl,
                    ),
                )
            }
        }
    }

    @Transactional
    fun resendInvitation(
        invitationId: Long,
        propertyOwnership: PropertyOwnership,
        invitingLandlord: Landlord,
    ): String {
        val invitation =
            invitationRepository
                .findById(invitationId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found") }

        if (invitation.registeredOwnership.id != propertyOwnership.id) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation does not belong to this property")
        }

        val email = invitation.invitedEmail
        val token = invitation.token

        invitationRepository.delete(invitation)
        invitationRepository.flush()

        invitationRepository.save(JointLandlordInvitation(token, email, propertyOwnership, invitingLandlord.name))
        val invitationUri = absoluteUrlProvider.buildJointLandlordInvitationUri(token.toString())

        invitationEmailSender.sendEmail(
            email,
            JointLandlordInvitationEmail(
                senderName = invitingLandlord.name,
                propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                invitationUri = invitationUri,
            ),
        )

        return email
    }

    fun getJourneyIdInvitationTokenPairsFromSession(): MutableList<Pair<String, String>>? =
        getListOfPairsFromSession(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS)

    fun addJourneyIdInvitationTokenPairToSession(
        journeyId: String,
        token: String,
    ) {
        val existingPairs =
            getListOfPairsFromSession<String, String>(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS)
                ?: mutableListOf()
        existingPairs.add(Pair(journeyId, token))
        session.setAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS, existingPairs)
    }

    fun getInvitationTokenForJourneyIdFromSession(journeyId: String): String =
        getJourneyIdInvitationTokenPairsFromSession()?.find { it.first == journeyId }?.second
            ?: throw PrsdbWebException("Invitation token not found in session for journey $journeyId")

    fun clearJourneyIdInvitationTokenPairsForTokenFromSession(token: String) {
        val remainingPairs = getJourneyIdInvitationTokenPairsFromSession()?.filter { pair -> pair.second != token }
        session.setAttribute(JOINT_LANDLORD_INVITATION_TOKEN_WITH_ACCEPTANCE_JOURNEY_IDS, remainingPairs)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T1, T2> getListOfPairsFromSession(sessionAttributeName: String): MutableList<Pair<T1, T2>>? =
        session.getAttribute(sessionAttributeName) as? MutableList<Pair<T1, T2>>

    fun getTokenIsValid(token: String): Boolean {
        val tokenUuid =
            try {
                UUID.fromString(token)
            } catch (_: IllegalArgumentException) {
                return false
            }

        val invitation = invitationRepository.findByToken(tokenUuid) ?: return false

        return invitation.status == JointLandlordInvitationStatus.PENDING
    }

    @Transactional
    fun hideExpiredInvitation(
        invitationId: Long,
        baseUserId: String,
    ) {
        val invitation =
            invitationRepository.findById(invitationId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation with id $invitationId was not found")
            }

        if (invitation.registeredOwnership.landlords.none { it.baseUser.id == baseUserId }) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authorized to modify this invitation")
        }

        if (invitation.status != JointLandlordInvitationStatus.EXPIRED) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Only expired invitations can be hidden")
        }

        invitation.isHidden = true
        invitationRepository.save(invitation)
    }

    private fun getInvitationById(id: Long): JointLandlordInvitation =
        invitationRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Joint landlord invitation not found")
        }

    fun getPendingInvitationIfAuthorizedLandlord(
        invitationId: Long,
        baseUserId: String,
    ): JointLandlordInvitation {
        val invitation = getInvitationById(invitationId)
        if (invitation.status != JointLandlordInvitationStatus.PENDING) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation is not pending")
        }
        val propertyOwnership = invitation.registeredOwnership
        if (propertyOwnership.landlords.none { it.baseUser.id == baseUserId }) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to cancel this invitation")
        }
        return invitation
    }

    @Transactional
    fun cancelInvitation(invitation: JointLandlordInvitation) {
        invitationRepository.delete(invitation)
    }

    fun addOrUpdateCancelledInvitationEmailInSession(cancelledEmail: String) {
        session.setAttribute(JOINT_LANDLORD_INVITATION_EMAIL_CANCELLED, cancelledEmail)
    }

    fun getCancelledInvitationEmailFromSession(): String? = session.getAttribute(JOINT_LANDLORD_INVITATION_EMAIL_CANCELLED) as? String

    fun getInvitationFromToken(token: String): JointLandlordInvitation =
        invitationRepository.findByToken(UUID.fromString(token))
            ?: throw EntityNotFoundException("No invitation found for token $token in the database")

    fun getInvitationForJourney(journeyId: String): JointLandlordInvitation =
        getInvitationFromToken(getInvitationTokenForJourneyIdFromSession(journeyId))

    fun addRejectedPropertyAddressToSession(propertyAddress: String) {
        session.setAttribute(JOINT_LANDLORD_INVITATION_REJECTION_PROPERTY_ADDRESS, propertyAddress)
    }

    fun getRejectedPropertyAddressFromSession(): String? =
        session.getAttribute(JOINT_LANDLORD_INVITATION_REJECTION_PROPERTY_ADDRESS) as? String

    fun storeLastAcceptedPropertyInSession(
        address: String,
        propertyOwnershipId: Long,
    ) {
        session.setAttribute(ACCEPTED_JOINT_LANDLORD_PROPERTY_DETAILS, Pair(address, propertyOwnershipId))
    }

    @Suppress("UNCHECKED_CAST")
    fun getLastAcceptedPropertyFromSession(): Pair<String, Long>? =
        session.getAttribute(ACCEPTED_JOINT_LANDLORD_PROPERTY_DETAILS) as? Pair<String, Long>
}
