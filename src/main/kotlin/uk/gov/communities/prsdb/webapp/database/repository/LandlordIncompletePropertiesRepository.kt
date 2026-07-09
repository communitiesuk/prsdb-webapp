package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import java.time.Instant

interface LandlordIncompletePropertiesRepository : JpaRepository<LandlordIncompleteProperties, Long> {
    @Suppress("ktlint:standard:function-naming")
    fun findBySavedJourneyState_CreatedDateBefore(
        cutoffDate: Instant,
        pageRequest: PageRequest,
    ): List<LandlordIncompleteProperties>

    @Suppress("ktlint:standard:function-naming")
    fun countBySavedJourneyState_CreatedDateBefore(cutoffDate: Instant): Long

    // TODO: PDJB-1275: Update assumption one base user per landlord
    // Once this is complete we should be able to remove the query
    @Query(
        "SELECT lip FROM LandlordIncompleteProperties lip " +
            "JOIN TREAT(lip.landlord AS IndividualLandlord) l " +
            "WHERE l.baseUser.id = :principalName",
    )
    @Suppress("ktlint:standard:function-naming")
    fun findByLandlord_BaseUser_Id(
        @Param("principalName") principalName: String,
        pageable: Pageable,
    ): Page<LandlordIncompleteProperties>
}
