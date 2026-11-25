package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import java.time.Instant

// The underscore tells JPA to access fields relating to the referenced table
@Suppress("ktlint:standard:function-naming")
interface PropertyOwnershipRepository :
    JpaRepository<PropertyOwnership, Long>,
    PropertyOwnershipSearchRepository {
    fun existsByIsActiveTrueAndAddress_Uprn(uprn: Long): Boolean

    fun existsByIsActiveTrueAndAddress_UprnAndIdNot(
        uprn: Long,
        id: Long,
    ): Boolean

    fun findAllByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlordId: Long): List<PropertyOwnership>

    fun findAllByOwnershipLinks_Landlord_IdAndIsActiveFalse(landlordId: Long): List<PropertyOwnership>

    fun findByRegistrationNumber_Number(registrationNumber: Long): PropertyOwnership?

    fun findByIdAndIsActiveTrue(id: Long): PropertyOwnership?

    fun findByIdAndIsActiveFalse(id: Long): PropertyOwnership?

    fun existsByIdAndIsActiveFalse(id: Long): Boolean

    // Postgres re-checks is_active after waiting for a concurrent claim's row lock, so only one claim can succeed.
    // Not clearAutomatically: that would detach entities the caller's transaction is still using.
    @Modifying(flushAutomatically = true)
    @Query("UPDATE PropertyOwnership po SET po.isActive = true WHERE po.id = :id AND po.isActive = false")
    fun claimForReactivation(
        @Param("id") id: Long,
    ): Int

    @Query(
        "SELECT po.id FROM PropertyOwnership po " +
            "LEFT JOIN po.propertyCompliance pc " +
            "WHERE po.isActive AND po.createdDate < :cutoff " +
            "AND (po.licenseProvideLater OR po.tenancyProvideLater " +
            "OR pc.gasSafetyCertProvideLater OR pc.electricalSafetyCertProvideLater " +
            "OR pc.epcProvideLater)",
    )
    fun findActivePropertyIdsWithAnyProvideLaterTaskCreatedBefore(
        @Param("cutoff") cutoff: Instant,
    ): List<Long>

    fun existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlordId: Long): Boolean

    fun existsByOwnershipLinks_Landlord_IdAndIsActiveFalse(landlordId: Long): Boolean

    fun existsByOwnershipLinks_Landlord_IdAndIsActiveTrueAndIdNot(
        landlordId: Long,
        id: Long,
    ): Boolean

    fun countByCreatedDateBetween(
        start: Instant,
        end: Instant,
    ): Long

    @Query(
        "SELECT COUNT(DISTINCT ol.landlord) FROM PropertyOwnership po " +
            "JOIN po.ownershipLinks ol " +
            "WHERE ol.createdDate BETWEEN :start AND :end",
    )
    fun countDistinctLandlordsWithOwnershipLinkCreatedBetween(
        @Param("start") start: Instant,
        @Param("end") end: Instant,
    ): Long

    @Query(
        "SELECT l.createdDate, MIN(ol.createdDate) FROM PropertyOwnership po " +
            "JOIN po.ownershipLinks ol " +
            "JOIN ol.landlord l " +
            "GROUP BY l.id, l.createdDate " +
            "HAVING MIN(ol.createdDate) BETWEEN :start AND :end",
    )
    fun findLandlordAndFirstOwnershipLinkCreatedDates(
        @Param("start") start: Instant,
        @Param("end") end: Instant,
    ): List<Array<Instant>>

    fun countByOwnershipLinks_Landlord_Id(landlordId: Long): Long
}
