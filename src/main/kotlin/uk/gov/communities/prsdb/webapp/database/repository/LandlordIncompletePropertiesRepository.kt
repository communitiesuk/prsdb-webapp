package uk.gov.communities.prsdb.webapp.database.repository

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
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

    @Suppress("ktlint:standard:function-naming")
    fun findBySavedJourneyState_JourneyIdAndUser_Id(
        journeyId: String,
        userId: String,
    ): LandlordIncompleteProperty?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT lip FROM LandlordIncompleteProperty lip WHERE lip.id = :id")
    fun findByIdForUpdate(id: LandlordIncompletePropertyId): LandlordIncompleteProperty?
}
