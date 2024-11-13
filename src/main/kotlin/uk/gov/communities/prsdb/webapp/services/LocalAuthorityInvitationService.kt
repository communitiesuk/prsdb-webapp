package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInvitationRepository
import java.net.URI
import java.util.UUID

@Service
class LocalAuthorityInvitationService(
    val invitationRepository: LocalAuthorityInvitationRepository,
    private val session: HttpSession,
) {
    fun createInvitationToken(
        email: String,
        authority: LocalAuthority,
    ): String {
        val token = UUID.randomUUID()
        invitationRepository.save(LocalAuthorityInvitation(token, email, authority))
        return token.toString()
    }

    fun getAuthorityForToken(token: String): LocalAuthority = getInvitationFromToken(token).invitingAuthority

    fun getEmailAddressForToken(token: String): String = getInvitationFromToken(token).invitedEmail

    fun getInvitationFromToken(token: String): LocalAuthorityInvitation {
        val tokenUuid = UUID.fromString(token)
        val invitation = invitationRepository.findByToken(tokenUuid) ?: throw Exception("Token not found in database")

        return invitation
    }

    fun tokenIsValid(token: String): Boolean {
        try {
            getInvitationFromToken(token)
        } catch (e: Exception) {
            return false
        }

        return true
    }

    fun buildInvitationUri(token: String): URI =
        MvcUriComponentsBuilder
            .fromMethodCall(on(RegisterLAUserController::class.java).acceptInvitation(token))
            .build()
            .toUri()

    fun storeTokenInSession(token: String) {
        session.setAttribute("token", token)
    }

    fun getTokenFromSession(): String? = session.getAttribute("token") as String?

    fun clearTokenFromSession() {
        session.setAttribute("token", "")
    }
}
