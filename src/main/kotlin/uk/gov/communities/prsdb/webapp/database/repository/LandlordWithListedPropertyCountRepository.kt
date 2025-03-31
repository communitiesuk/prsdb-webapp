package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LandlordWithListedPropertyCount

interface LandlordWithListedPropertyCountRepository : JpaRepository<LandlordWithListedPropertyCount?, Long?> {
    fun findByLandlordIdIn(primaryLandlordId: List<Long>): List<LandlordWithListedPropertyCount>

    @Suppress("ktlint:standard:function-naming")
    fun findByLandlord_BaseUser_Id(subjectId: String): LandlordWithListedPropertyCount?
}
