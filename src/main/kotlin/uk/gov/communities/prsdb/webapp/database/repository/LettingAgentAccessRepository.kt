package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import java.util.UUID

interface LettingAgentAccessRepository : JpaRepository<LettingAgentAccess, Long> {
    fun findByToken(token: UUID): LettingAgentAccess?

    fun findByPropertyOwnershipId(propertyOwnershipId: Long): LettingAgentAccess?

    fun deleteByPropertyOwnershipId(propertyOwnershipId: Long)
}
