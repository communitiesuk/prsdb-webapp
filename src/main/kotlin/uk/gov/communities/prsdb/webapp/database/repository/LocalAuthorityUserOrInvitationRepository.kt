package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUserOrInvitation

interface LocalAuthorityUserOrInvitationRepository : JpaRepository<LocalAuthorityUserOrInvitation?, Long?> {
    fun findByLocalAuthority(
        localAuthority: LocalAuthority,
        pageable: Pageable,
    ): Page<LocalAuthorityUserOrInvitation>

    @Query(
        "SELECT u " +
            "FROM LocalAuthorityUserOrInvitation u " +
            "WHERE u.localAuthority = :localAuthority " +
            "AND NOT (u.entityType = 'local_authority_invitation' AND u.isManager = true)",
    )
    fun findByLocalAuthorityNotIncludingAdminInvitations(
        localAuthority: LocalAuthority,
        pageable: Pageable,
    ): Page<LocalAuthorityUserOrInvitation>

    fun findAllByIsManagerTrue(pageable: Pageable): Page<LocalAuthorityUserOrInvitation>
}
