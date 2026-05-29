package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

@Suppress("ktlint:standard:function-naming")
interface PropertyOwnershipRepository :
    JpaRepository<PropertyOwnership, Long>,
    PropertyOwnershipSearchRepository {
    fun existsByLandlordship_IsActiveTrueAndPropertyDetails_Address_Uprn(uprn: Long): Boolean

    fun findAllByLandlordship_PrimaryLandlord_BaseUser_IdAndLandlordship_IsActiveTrue(userId: String): List<PropertyOwnership>

    fun findAllByLandlordship_PrimaryLandlord_IdAndLandlordship_IsActiveTrue(landlordId: Long): List<PropertyOwnership>

    fun findByLandlordship_RegistrationNumber_Number(registrationNumber: Long): PropertyOwnership?

    fun findByIdAndLandlordship_IsActiveTrue(id: Long): PropertyOwnership?

    fun existsByLandlordship_PrimaryLandlord_BaseUser_IdAndLandlordship_IsActiveTrue(userId: String): Boolean

    fun existsByLandlordship_PrimaryLandlord_BaseUser_IdAndLandlordship_IsActiveTrueAndPropertyDetails_Address_Uprn(
        userId: String,
        uprn: Long,
    ): Boolean

    fun countByLandlordship_PrimaryLandlord_BaseUser_Id(userId: String): Long
}
