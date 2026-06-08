package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

// The underscore tells JPA to access fields relating to the referenced table
@Suppress("ktlint:standard:function-naming")
interface PropertyOwnershipRepository :
    JpaRepository<PropertyOwnership, Long>,
    PropertyOwnershipSearchRepository {
    fun existsByIsActiveTrueAndAddress_Uprn(uprn: Long): Boolean

    fun findAllByLandlords_BaseUser_IdAndIsActiveTrue(userId: String): List<PropertyOwnership>

    fun findAllByLandlords_IdAndIsActiveTrue(landlordId: Long): List<PropertyOwnership>

    fun findByRegistrationNumber_Number(registrationNumber: Long): PropertyOwnership?

    fun findByIdAndIsActiveTrue(id: Long): PropertyOwnership?

    fun existsByLandlords_BaseUser_IdAndIsActiveTrue(userId: String): Boolean

    fun existsByLandlords_BaseUser_IdAndIsActiveTrueAndAddress_Uprn(
        userId: String,
        uprn: Long,
    ): Boolean

    fun countByLandlords_BaseUser_Id(userId: String): Long
}
