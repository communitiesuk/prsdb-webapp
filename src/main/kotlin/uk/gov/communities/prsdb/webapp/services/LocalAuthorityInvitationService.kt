package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import org.springframework.ui.ExtendedModelMap
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on
import uk.gov.communities.prsdb.webapp.controllers.ExampleInvitationTokenController
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInvitationRepository
import java.net.URI
import java.util.UUID

@Service
class LocalAuthorityInvitationService(
    val invitationRepository: LocalAuthorityInvitationRepository,
) {
    fun createInvitationToken(
        email: String,
        authority: LocalAuthority,
    ): String {
        val token = UUID.randomUUID()
        invitationRepository.save(LocalAuthorityInvitation(token, email, authority))
        return token.toString()
    }

    fun getAuthorityForToken(token: String): LocalAuthority {
        val tokenUuid = UUID.fromString(token)
        return invitationRepository.findByToken(tokenUuid).invitingAuthority
    }

    // TODO-405: This function call should be set to match the controller method on the new invitation controller once created
    fun buildInvitationUri(token: String): URI =
        MvcUriComponentsBuilder
            .fromMethodCall(on(ExampleInvitationTokenController::class.java).acceptInvitation(ExtendedModelMap(), token))
            .build()
            .toUri()
}
