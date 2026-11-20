package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import java.time.Instant

interface LandlordDeactivationRepository : JpaRepository<Landlord, Long> {
    // The active property check is defensive: landlords who never registered a property can't have one
    @Query(
        "SELECT l.id FROM Landlord l " +
            "WHERE l.isActive = true " +
            "AND l.hasRegisteredProperty = false " +
            "AND l.createdDate < :cutoff " +
            "AND NOT EXISTS (" +
            "SELECT ol.id FROM OwnershipLink ol WHERE ol.landlord = l AND ol.propertyOwnership.isActive = true" +
            ")",
    )
    fun findIdsOfActiveLandlordsWithNoRegisteredPropertyCreatedBefore(
        @Param("cutoff") cutoff: Instant,
    ): List<Long>

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
