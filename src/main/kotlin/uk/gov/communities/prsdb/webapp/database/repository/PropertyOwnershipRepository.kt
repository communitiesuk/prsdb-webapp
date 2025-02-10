package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    // TODO PRSD-660: Add filtering to searchMatchingX queries
    @Query(
        "SELECT po.* " +
            "FROM property_ownership po " +
            "JOIN registration_number r ON po.registration_number_id = r.id " +
            "WHERE po.is_active AND r.number = :searchPRN",
        nativeQuery = true,
    )
    fun searchMatchingPRN(
        @Param("searchPRN") searchPRN: Long,
        pageable: Pageable,
    ): Page<PropertyOwnership>

    @Query(
        "SELECT po.* " +
            "FROM property_ownership po " +
            "JOIN property p ON po.property_id = p.id " +
            "JOIN address a ON p.address_id = a.id " +
            "WHERE po.is_active AND a.uprn = :searchUPRN",
        nativeQuery = true,
    )
    fun searchMatchingUPRN(
        @Param("searchUPRN") searchUPRN: Long,
        pageable: Pageable,
    ): Page<PropertyOwnership>

    @Query(
        "SELECT po.* " +
            "FROM property_ownership po " +
            "JOIN property p ON po.property_id = p.id " +
            "JOIN address a ON p.address_id = a.id " +
            "WHERE po.is_active AND a.single_line_address %> :searchTerm " +
            "ORDER BY a.single_line_address <->> :searchTerm",
        nativeQuery = true,
    )
    fun searchMatching(
        @Param("searchTerm") searchTerm: String,
        pageable: Pageable,
    ): Page<PropertyOwnership>
}
