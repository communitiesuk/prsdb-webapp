package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import java.time.Instant

// The underscore tells JPA to access fields relating to the referenced table
@Suppress("ktlint:standard:function-naming")
interface LandlordRepository :
    JpaRepository<Landlord, Long>,
    LandlordSearchRepository {
    fun findAllByIsActiveTrueAndHasRegisteredPropertyFalseAndCreatedDateBefore(cutoffDate: Instant): List<Landlord>

    @Query(
        "SELECT l.id FROM Landlord l " +
            "WHERE l.isActive = true " +
            "AND l.hasRegisteredProperty = true " +
            "AND NOT EXISTS (" +
            "SELECT ol.id FROM OwnershipLink ol WHERE ol.landlord = l AND ol.propertyOwnership.isActive = true" +
            ")",
    )
    fun findIdsOfActiveLandlordsWithRegisteredPropertyButNoActiveProperties(): List<Long>
}
