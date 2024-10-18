package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInvitationRepository
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
}
