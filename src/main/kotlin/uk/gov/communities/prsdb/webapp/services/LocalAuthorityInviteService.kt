package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvite
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInviteRepository
import java.util.UUID

@Service
class LocalAuthorityInviteService(
    val inviteRepository: LocalAuthorityInviteRepository,
) {
    fun createInviteToken(authority: LocalAuthority): String {
        val token = UUID.randomUUID()
        inviteRepository.save(LocalAuthorityInvite(token, authority))
        return token.toString()
    }

    fun getAuthorityForToken(token: String): LocalAuthority {
        val tokenUuid = UUID.fromString(token)
        return inviteRepository.findByToken(tokenUuid).invitingAuthority
    }
}
