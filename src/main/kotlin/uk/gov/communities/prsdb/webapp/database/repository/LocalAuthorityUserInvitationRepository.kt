package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUserInvitation

interface LocalAuthorityUserInvitationRepository : JpaRepository<LocalAuthorityUserInvitation, Long?> {
    @Suppress("ktlint:standard:function-naming")
    fun findByLocalAuthority(localAuthority: LocalAuthority): List<LocalAuthorityUserInvitation>
}
