package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber

interface RegistrationNumberRepository : JpaRepository<RegistrationNumber?, Long?> {
    fun existsByNumber(number: Long): Boolean
}
