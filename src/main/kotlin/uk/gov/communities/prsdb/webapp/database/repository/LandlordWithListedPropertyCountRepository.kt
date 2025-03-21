package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LandlordWithListedPropertyCount

interface LandlordWithListedPropertyCountRepository : JpaRepository<LandlordWithListedPropertyCount?, Long?> {
    fun findByLandlordIdIn(primaryLandlordId: List<Long>): List<LandlordWithListedPropertyCount>
}
