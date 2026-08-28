package uk.gov.communities.prsdb.webapp.database.repository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import java.util.UUID

interface LettingAgentAccessRepository : JpaRepository<LettingAgentAccess, Long> {
    fun findByToken(token: UUID): LettingAgentAccess?

    fun findByPropertyOwnershipId(propertyOwnershipId: Long): LettingAgentAccess?

    fun deleteByPropertyOwnershipId(propertyOwnershipId: Long)

    @Modifying
    @Transactional
    @Query(
        "UPDATE LettingAgentAccess a SET a.encodedPassword = :encodedPassword " +
            "WHERE a.id = :id AND a.encodedPassword IS NULL",
    )
    fun setEncodedPasswordIfAbsent(
        id: Long,
        encodedPassword: String,
    ): Int
}
