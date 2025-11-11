package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilUserOrInvitation

interface LocalAuthorityUserOrInvitationRepository : JpaRepository<LocalCouncilUserOrInvitation?, Long?> {
    fun findByLocalAuthority(
        localCouncil: LocalCouncil,
        pageable: Pageable,
    ): Page<LocalCouncilUserOrInvitation>

    @Query(
        "SELECT u " +
            "FROM LocalCouncilUserOrInvitation u " +
            "WHERE u.localCouncil = :localCouncil " +
            "AND NOT (u.entityType = 'local_authority_invitation' AND u.isManager = true)",
    )
    fun findByLocalAuthorityNotIncludingAdminInvitations(
        localCouncil: LocalCouncil,
        pageable: Pageable,
    ): Page<LocalCouncilUserOrInvitation>

    fun findAllByIsManagerTrue(pageable: Pageable): Page<LocalCouncilUserOrInvitation>
}
