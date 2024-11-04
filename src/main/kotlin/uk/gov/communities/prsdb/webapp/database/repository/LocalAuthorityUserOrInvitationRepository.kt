package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUserOrInvitation

interface LocalAuthorityUserOrInvitationRepository : JpaRepository<LocalAuthorityUserOrInvitation?, Long?> {
    fun findByLocalAuthority(
        localAuthority: LocalAuthority,
        pageable: Pageable,
    ): Page<LocalAuthorityUserOrInvitation>
}
