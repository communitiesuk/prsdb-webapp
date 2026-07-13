package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LandlordAccessLink

// TODO: PDJB-1275: Move all landlord access checking to use this table
// Currently some of it is based on individual_subject_identifier in the landlord table
@Suppress("ktlint:standard:function-naming")
interface LandlordAccessLinkRepository : JpaRepository<LandlordAccessLink, Long> {
    fun findByBaseUser_Id(baseUserId: String): List<LandlordAccessLink>

    fun existsByBaseUser_IdAndLandlord_Id(
        baseUserId: String,
        landlordId: Long,
    ): Boolean
}
