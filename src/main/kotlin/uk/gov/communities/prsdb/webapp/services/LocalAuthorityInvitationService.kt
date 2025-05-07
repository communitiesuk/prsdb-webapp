package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.LA_USER_INVITATION_TOKEN
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.InvalidTokenException
import java.util.UUID

@Service
class LocalAuthorityInvitationService(
    val invitationRepository: LocalAuthorityInvitationRepository,
    private val session: HttpSession,
) {
    fun createInvitationToken(
        email: String,
        authority: LocalAuthority,
        invitedAsAdmin: Boolean = false,
    ): String {
        val token = UUID.randomUUID()
        invitationRepository.save(LocalAuthorityInvitation(token, email, authority, invitedAsAdmin))
        return token.toString()
    }

    fun getAuthorityForToken(token: String): LocalAuthority = getInvitationFromToken(token).invitingAuthority

    fun getEmailAddressForToken(token: String): String = getInvitationFromToken(token).invitedEmail

    fun getInvitationFromToken(token: String): LocalAuthorityInvitation {
        val tokenUuid = UUID.fromString(token)
        val invitation = invitationRepository.findByToken(tokenUuid) ?: throw InvalidTokenException("Token not found in database")

        return invitation
    }

    fun deleteInvitation(invitation: LocalAuthorityInvitation) {
        invitationRepository.delete(invitation)
    }

    fun deleteInvitation(invitationId: Long) {
        invitationRepository.deleteById(invitationId)
    }

    fun tokenIsValid(token: String): Boolean {
        try {
            getInvitationFromToken(token)
        } catch (e: InvalidTokenException) {
            return false
        }

        return true
    }

    fun storeTokenInSession(token: String) {
        session.setAttribute(LA_USER_INVITATION_TOKEN, token)
    }

    fun getTokenFromSession(): String? = session.getAttribute(LA_USER_INVITATION_TOKEN) as String?

    fun clearTokenFromSession() {
        session.setAttribute(LA_USER_INVITATION_TOKEN, null)
    }

    fun getInvitationById(id: Long): LocalAuthorityInvitation = invitationRepository.getReferenceById(id)
}
