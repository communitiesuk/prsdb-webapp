package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_HOURS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_TOKEN
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.TokenNotFoundException
import java.util.UUID
import kotlin.time.Duration.Companion.hours

@PrsdbWebService
class JointLandlordInvitationService(
    val invitationRepository: JointLandlordInvitationRepository,
    private val session: HttpSession,
) {
    fun createInvitationToken(
        email: String,
        propertyOwnership: PropertyOwnership,
    ): String {
        val token = UUID.randomUUID()
        invitationRepository.save(JointLandlordInvitation(token, email, propertyOwnership))
        return token.toString()
    }

    fun getPropertyOwnershipForToken(token: String): PropertyOwnership = getInvitationFromToken(token).registeredPropertyID

    fun getEmailAddressForToken(token: String): String = getInvitationFromToken(token).invitedEmail

    fun getInvitationFromToken(token: String): JointLandlordInvitation {
        val tokenUuid = UUID.fromString(token)
        val invitation =
            invitationRepository.findByToken(tokenUuid) ?: throw TokenNotFoundException("Invitation token not found in database")

        return invitation
    }

    fun deleteInvitation(invitation: JointLandlordInvitation) {
        invitationRepository.delete(invitation)
    }

    fun deleteInvitation(invitationId: Long) {
        invitationRepository.deleteById(invitationId)
    }

    fun tokenIsValid(token: String): Boolean {
        try {
            val invitation = getInvitationFromToken(token)
            return !getInvitationHasExpired(invitation)
        } catch (e: TokenNotFoundException) {
            return false
        }
    }

    fun getInvitationOrNull(token: String): JointLandlordInvitation? =
        try {
            getInvitationFromToken(token)
        } catch (e: TokenNotFoundException) {
            null
        }

    fun storeTokenInSession(token: String) {
        session.setAttribute(JOINT_LANDLORD_INVITATION_TOKEN, token)
    }

    fun getTokenFromSession(): String? = session.getAttribute(JOINT_LANDLORD_INVITATION_TOKEN) as String?

    fun clearTokenFromSession() {
        session.removeAttribute(JOINT_LANDLORD_INVITATION_TOKEN)
    }

    fun getInvitationByIdOrNull(id: Long): JointLandlordInvitation? = invitationRepository.findById(id).orElse(null)

    fun throwErrorIfInvitationExists(invitation: JointLandlordInvitation) {
        if (invitationRepository.existsById(invitation.id)) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Invitation with id ${invitation.id} is still in the joint_landlord_invitation table",
            )
        }
    }

    fun getInvitationHasExpired(invitation: JointLandlordInvitation): Boolean {
        val expiresAtInstant =
            invitation.createdDate
                .toKotlinInstant()
                .plus(JOINT_LANDLORD_INVITATION_LIFETIME_IN_HOURS.hours)

        return Clock.System.now() > expiresAtInstant
    }
}
