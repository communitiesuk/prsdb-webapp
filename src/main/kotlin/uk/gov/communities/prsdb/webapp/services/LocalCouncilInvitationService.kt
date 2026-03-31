package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_INVITATION_LIFETIME_IN_HOURS
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_USER_INVITATION_TOKEN
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.InvalidInvitationException
import uk.gov.communities.prsdb.webapp.exceptions.TokenNotFoundException
import java.util.UUID
import kotlin.time.Duration.Companion.hours

@PrsdbWebService
class LocalCouncilInvitationService(
    val invitationRepository: LocalCouncilInvitationRepository,
    private val session: HttpSession,
) {
    fun createInvitationToken(
        email: String,
        council: LocalCouncil,
        invitedAsAdmin: Boolean = false,
    ): String {
        val token = UUID.randomUUID()
        invitationRepository.save(LocalCouncilInvitation(token, email, council, invitedAsAdmin))
        return token.toString()
    }

    fun getAuthorityForToken(token: String): LocalCouncil = getInvitationFromToken(token).invitingCouncil

    fun getEmailAddressForToken(token: String): String = getInvitationFromToken(token).invitedEmail

    fun getInvitationFromToken(token: String): LocalCouncilInvitation {
        val tokenUuid = UUID.fromString(token)
        val invitation =
            invitationRepository.findByToken(tokenUuid) ?: throw TokenNotFoundException("Invitation token not found in database")

        return invitation
    }

    fun deleteInvitation(invitation: LocalCouncilInvitation) {
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

    fun getInvitationOrNull(token: String): LocalCouncilInvitation? =
        try {
            getInvitationFromToken(token)
        } catch (e: TokenNotFoundException) {
            null
        }

    fun storeTokenInSession(token: String) {
        session.setAttribute(LOCAL_COUNCIL_USER_INVITATION_TOKEN, token)
    }

    fun getTokenFromSession(): String? = session.getAttribute(LOCAL_COUNCIL_USER_INVITATION_TOKEN) as String?

    fun clearTokenFromSession() {
        session.setAttribute(LOCAL_COUNCIL_USER_INVITATION_TOKEN, null)
    }

    fun getInvitationByIdOrNull(id: Long): LocalCouncilInvitation? = invitationRepository.findById(id).orElse(null)

    fun throwErrorIfInvitationExists(invitation: LocalCouncilInvitation) {
        if (invitationRepository.existsById(invitation.id)) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Invitation with id ${invitation.id} is still in the local_council_invitations table",
            )
        }
    }

    fun getAdminInvitationByIdOrNull(id: Long): LocalCouncilInvitation? {
        val invitation = getInvitationByIdOrNull(id)
        return if (invitation?.invitedAsAdmin == true) {
            invitation
        } else {
            null
        }
    }

    fun getInvitationHasExpired(invitation: LocalCouncilInvitation): Boolean {
        val expiresAtInstant =
            invitation.createdDate
                .toKotlinInstant()
                .plus(LOCAL_COUNCIL_INVITATION_LIFETIME_IN_HOURS.hours)

        return Clock.System.now() > expiresAtInstant
    }

    fun getValidInvitationFromToken(token: String): LocalCouncilInvitation {
        val invitation =
            getInvitationOrNull(token)
                ?: throw InvalidInvitationException("Invitation not found for token")

        if (getInvitationHasExpired(invitation)) {
            throw InvalidInvitationException("Invitation has expired")
        }

        return invitation
    }
}
