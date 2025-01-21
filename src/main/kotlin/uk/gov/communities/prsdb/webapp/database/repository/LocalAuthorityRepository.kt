package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority

interface LocalAuthorityRepository : JpaRepository<LocalAuthority, Int> {
    fun findByCustodianCode(custodianCode: String): LocalAuthority?

    fun findAllByOrderByNameAsc(): List<LocalAuthority>
}
