package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperty
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompletePropertyId
import java.time.Instant

interface LandlordIncompletePropertiesRepository : JpaRepository<LandlordIncompleteProperty, LandlordIncompletePropertyId> {
    @Suppress("ktlint:standard:function-naming")
    fun findBySavedJourneyState_CreatedDateBefore(
        cutoffDate: Instant,
        pageRequest: PageRequest,
    ): List<LandlordIncompleteProperty>

    @Suppress("ktlint:standard:function-naming")
    fun countBySavedJourneyState_CreatedDateBefore(cutoffDate: Instant): Long

    @Suppress("ktlint:standard:function-naming")
    fun findByUser_Id(
        userId: String,
        pageable: Pageable,
    ): Page<LandlordIncompleteProperty>

    @Suppress("ktlint:standard:function-naming")
    fun countByUser_Id(userId: String): Long
}
