package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import java.util.UUID

interface LocalAuthorityInvitationRepository : JpaRepository<LocalAuthorityInvitation?, UUID?> {
    fun findByToken(token: UUID): LocalAuthorityInvitation

    fun findByInvitingAuthorityOrderByInvitedEmail(localAuthority: LocalAuthority): List<LocalAuthorityInvitation>
}
