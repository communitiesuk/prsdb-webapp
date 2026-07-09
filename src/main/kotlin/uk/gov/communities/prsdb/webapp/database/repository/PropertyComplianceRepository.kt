package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

@Suppress("ktlint:standard:function-naming")
interface PropertyComplianceRepository : JpaRepository<PropertyCompliance, Long> {
    fun findByPropertyOwnership_Id(propertyOwnershipId: Long): PropertyCompliance?

    // TODO: PDJB-1275: Update assumption one base user per landlord
    // Once this is complete we should be able to remove the query
    @Query(
        "SELECT pc FROM PropertyCompliance pc " +
            "JOIN pc.propertyOwnership po " +
            "JOIN po.ownershipLinks ol " +
            "JOIN TREAT(ol.landlord AS IndividualLandlord) l " +
            "WHERE l.baseUser.id = :landlordBaseUserId",
    )
    fun findAllByPropertyOwnership_OwnershipLinks_Landlord_BaseUser_Id(
        @Param("landlordBaseUserId") landlordBaseUserId: String,
    ): List<PropertyCompliance>

    fun deleteByPropertyOwnership_IdIn(propertyOwnershipIds: List<Long>)

    fun deleteByPropertyOwnership_Id(propertyOwnershipId: Long)
}
