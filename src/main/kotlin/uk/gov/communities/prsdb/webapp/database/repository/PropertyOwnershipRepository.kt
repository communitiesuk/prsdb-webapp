package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

interface PropertyOwnershipRepository : JpaRepository<PropertyOwnership, Long> {
    // The underscore tells JPA to access fields relating to the referenced table
    @Suppress("ktlint:standard:function-naming")
    fun existsByIsActiveTrueAndProperty_Id(id: Long): Boolean

    // This returns all active PropertyOwnerships for a given landlord from their baseUser_Id with a particular RegistrationStatus
    @Suppress("ktlint:standard:function-naming")
    fun findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_Status(
        userId: String,
        status: RegistrationStatus,
    ): List<PropertyOwnership>

    @Suppress("ktlint:standard:function-naming")
    fun findByRegistrationNumber_Number(registrationNumber: Long): PropertyOwnership?

    // This returns all active PropertyOwnerships for a given landlord from their landlord_Id with a particular RegistrationStatus
    @Suppress("ktlint:standard:function-naming")
    fun findAllByPrimaryLandlord_IdAndIsActiveTrueAndProperty_Status(
        landlordId: Long,
        status: RegistrationStatus,
    ): List<PropertyOwnership>

    fun findByIdAndIsActiveTrue(id: Long): PropertyOwnership?
}
