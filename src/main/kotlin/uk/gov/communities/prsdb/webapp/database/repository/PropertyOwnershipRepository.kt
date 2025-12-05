package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

// The underscore tells JPA to access fields relating to the referenced table
@Suppress("ktlint:standard:function-naming")
interface PropertyOwnershipRepository :
    JpaRepository<PropertyOwnership, Long>,
    PropertyOwnershipSearchRepository {
    fun existsByIsActiveTrueAndAddress_Uprn(uprn: Long): Boolean

    fun countByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndCurrentNumTenantsIsGreaterThanAndIncompleteComplianceFormNotNull(
        userId: String,
        currentNumTenantsIsGreaterThan: Int,
    ): Int

    fun findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(userId: String): List<PropertyOwnership>

    fun findAllByPrimaryLandlord_IdAndIsActiveTrue(landlordId: Long): List<PropertyOwnership>

    fun findByRegistrationNumber_Number(registrationNumber: Long): PropertyOwnership?

    fun findByIdAndIsActiveTrue(id: Long): PropertyOwnership?

    fun existsByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(userId: String): Boolean
}
