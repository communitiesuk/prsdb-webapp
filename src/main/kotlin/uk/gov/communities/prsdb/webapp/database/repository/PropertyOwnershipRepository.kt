package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

interface PropertyOwnershipRepository : JpaRepository<PropertyOwnership, Long> {
    // The underscore tells JPA to access fields relating to the referenced table
    @Suppress("ktlint:standard:function-naming")
    fun existsByIsActiveTrueAndProperty_Id(id: Long): Boolean

    // This returns all active PropertyOwnerships for a given landlord with a particular RegistrationStatus
    @Suppress("ktlint:standard:function-naming")
    fun findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_Status(
        userId: String,
        status: RegistrationStatus,
    ): List<PropertyOwnership>

    fun countByPrimaryLandlord(primaryLandlord: Landlord): Int

    @Query(
        "SELECT COUNT(primary_landlord_id) " +
            "FROM property_ownership po  " +
            "WHERE po.primary_landlord_id IN (:primaryLandlordIds)" +
            "GROUP BY po.primary_landlord_id",
        nativeQuery = true,
    )
    fun countListedProperties(primaryLandlordIds: List<Long>): List<Int>
}
