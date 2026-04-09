package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil

interface LocalCouncilRepository : JpaRepository<LocalCouncil, Int> {
    fun findByCustodianCode(custodianCode: String): LocalCouncil?

    fun findAllByOrderByNameAsc(): List<LocalCouncil>

    @Query("SELECT id FROM local_council", nativeQuery = true)
    fun findAllId(): List<Int>
}
