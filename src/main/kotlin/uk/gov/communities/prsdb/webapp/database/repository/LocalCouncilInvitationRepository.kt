package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import java.util.UUID

interface LocalCouncilInvitationRepository : JpaRepository<LocalCouncilInvitation?, Long?> {
    fun findByToken(token: UUID): LocalCouncilInvitation?

    fun findByInvitingCouncil(
        localCouncil: LocalCouncil,
        pageRequest: PageRequest,
    ): List<LocalCouncilInvitation>

    fun countByInvitingCouncil(invitingAuthority: LocalCouncil): Long
}
