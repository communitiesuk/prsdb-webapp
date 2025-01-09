package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import java.util.UUID

interface LocalAuthorityInvitationRepository : JpaRepository<LocalAuthorityInvitation?, Long?> {
    fun findByToken(token: UUID): LocalAuthorityInvitation?

    fun findByInvitingAuthority(
        localAuthority: LocalAuthority,
        pageRequest: PageRequest,
    ): List<LocalAuthorityInvitation>

    fun countByInvitingAuthority(invitingAuthority: LocalAuthority): Long
}
