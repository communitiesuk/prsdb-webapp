package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
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

    fun findAllByOwnershipLinks_Landlord_BaseUser_IdAndIsActiveTrue(userId: String): List<PropertyOwnership>

    fun findAllByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlordId: Long): List<PropertyOwnership>

    fun findByRegistrationNumber_Number(registrationNumber: Long): PropertyOwnership?

    fun findByIdAndIsActiveTrue(id: Long): PropertyOwnership?

    fun existsByOwnershipLinks_Landlord_BaseUser_IdAndIsActiveTrue(userId: String): Boolean

    fun existsByOwnershipLinks_Landlord_BaseUser_IdAndIsActiveTrueAndAddress_Uprn(
        userId: String,
        uprn: Long,
    ): Boolean

    fun countByCreatedDateBetween(
        start: Instant,
        end: Instant,
    ): Long

    @Query(
        "SELECT COUNT(DISTINCT ol.landlord) FROM PropertyOwnership po " +
            "JOIN po.ownershipLinks ol " +
            "WHERE po.createdDate BETWEEN :start AND :end",
    )
    fun countDistinctLandlordsWithPropertyCreatedBetween(
        @Param("start") start: Instant,
        @Param("end") end: Instant,
    ): Long

    @Query(
        "SELECT l.createdDate, MIN(po.createdDate) FROM PropertyOwnership po " +
            "JOIN po.ownershipLinks ol " +
            "JOIN ol.landlord l " +
            "GROUP BY l.id, l.createdDate " +
            "HAVING MIN(po.createdDate) BETWEEN :start AND :end",
    )
    fun findLandlordAndFirstPropertyCreatedDates(
        @Param("start") start: Instant,
        @Param("end") end: Instant,
    ): List<Array<Instant>>

    fun countByOwnershipLinks_Landlord_BaseUser_Id(userId: String): Long
}
