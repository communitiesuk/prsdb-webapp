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

    fun findAllByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlordId: Long): List<PropertyOwnership>

    fun findByRegistrationNumber_Number(registrationNumber: Long): PropertyOwnership?

    fun findByIdAndIsActiveTrue(id: Long): PropertyOwnership?

    // TODO: PDJB-1275: Update assumption one base user per landlord
    // Once this is complete we should be able to remove the query
    @Query(
        "SELECT CASE WHEN COUNT(po) > 0 THEN true ELSE false END FROM PropertyOwnership po " +
            "JOIN po.ownershipLinks ol " +
            "JOIN TREAT(ol.landlord AS IndividualLandlord) l " +
            "WHERE l.baseUser.id = :userId AND po.isActive = true",
    )
    fun existsByOwnershipLinks_Landlord_BaseUser_IdAndIsActiveTrue(
        @Param("userId") userId: String,
    ): Boolean

    // TODO: PDJB-1275: Update assumption one base user per landlord
    // Once this is complete we should be able to remove the query
    @Query(
        "SELECT CASE WHEN COUNT(po) > 0 THEN true ELSE false END FROM PropertyOwnership po " +
            "JOIN po.ownershipLinks ol " +
            "JOIN TREAT(ol.landlord AS IndividualLandlord) l " +
            "WHERE l.baseUser.id = :userId AND po.isActive = true AND po.address.uprn = :uprn",
    )
    fun existsByOwnershipLinks_Landlord_BaseUser_IdAndIsActiveTrueAndAddress_Uprn(
        @Param("userId") userId: String,
        @Param("uprn") uprn: Long,
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

    // TODO: PDJB-1275: Update assumption one base user per landlord
    // Once this is complete we should be able to remove the query
    @Query(
        "SELECT COUNT(po) FROM PropertyOwnership po " +
            "JOIN po.ownershipLinks ol " +
            "JOIN TREAT(ol.landlord AS IndividualLandlord) l " +
            "WHERE l.baseUser.id = :userId",
    )
    fun countByOwnershipLinks_Landlord_BaseUser_Id(
        @Param("userId") userId: String,
    ): Long
}
