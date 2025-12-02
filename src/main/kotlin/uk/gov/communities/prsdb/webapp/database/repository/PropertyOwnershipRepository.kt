package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

// The underscore tells JPA to access fields relating to the referenced table
@Suppress("ktlint:standard:function-naming")
interface PropertyOwnershipRepository : JpaRepository<PropertyOwnership, Long> {
    fun existsByIsActiveTrueAndAddress_Uprn(uprn: Long): Boolean

    fun countByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndCurrentNumTenantsIsGreaterThanAndIncompleteComplianceFormNotNull(
        userId: String,
        currentNumTenantsIsGreaterThan: Int,
    ): Int

    fun findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrue(userId: String): List<PropertyOwnership>

    fun findAllByPrimaryLandlord_IdAndIsActiveTrue(landlordId: Long): List<PropertyOwnership>

    fun findByRegistrationNumber_Number(registrationNumber: Long): PropertyOwnership?

    fun findByIdAndIsActiveTrue(id: Long): PropertyOwnership?

    @Query(
        "SELECT po.* " +
            "FROM property_ownership po " +
            "JOIN registration_number r ON po.registration_number_id = r.id " +
            "WHERE po.is_active AND r.number = :searchPRN " +
            FILTERS,
        nativeQuery = true,
    )
    fun searchMatchingPRN(
        @Param("searchPRN") searchPRN: Long,
        @Param("localCouncilUserBaseId") localCouncilUserBaseId: String,
        @Param("restrictToLocalCouncil") restrictToLocalCouncil: Boolean = false,
        @Param("restrictToLicenses") restrictToLicenses: Collection<LicensingType> = LicensingType.entries,
        pageable: Pageable,
    ): Page<PropertyOwnership>

    @Query(
        "SELECT po.* " +
            "FROM property_ownership po " +
            "JOIN address a ON po.address_id = a.id " +
            "WHERE po.is_active AND a.uprn = :searchUPRN " +
            FILTERS,
        nativeQuery = true,
    )
    fun searchMatchingUPRN(
        @Param("searchUPRN") searchUPRN: Long,
        @Param("localCouncilUserBaseId") localCouncilUserBaseId: String,
        @Param("restrictToLocalCouncil") restrictToLocalCouncil: Boolean = false,
        @Param("restrictToLicenses") restrictToLicenses: Collection<LicensingType> = LicensingType.entries,
        pageable: Pageable,
    ): Page<PropertyOwnership>

    @Query(
        "SELECT po.* " +
            "FROM property_ownership po " +
            "WHERE po.single_line_address %>> :searchTerm " +
            "AND po.is_active " +
            FILTERS +
            "ORDER BY po.single_line_address <->>> :searchTerm",
        nativeQuery = true,
    )
    fun searchMatching(
        @Param("searchTerm") searchTerm: String,
        @Param("localCouncilUserBaseId") localCouncilUserBaseId: String,
        @Param("restrictToLocalCouncil") restrictToLocalCouncil: Boolean = false,
        @Param("restrictToLicenses") restrictToLicenses: Collection<LicensingType> = LicensingType.entries,
        pageable: Pageable,
    ): Page<PropertyOwnership>

    companion object {
        private const val NO_LICENCE_TYPE =
            "#{T(uk.gov.communities.prsdb.webapp.constants.enums.LicensingType).NO_LICENSING}"

        // Determines whether the property's address is in the LA user's LA
        private const val LOCAL_COUNCIL_FILTER =
            """
            AND ((SELECT a.local_council_id 
                  FROM address a 
                  WHERE po.address_id = a.id)
                 =
                 (SELECT lcu.local_council_id 
                  FROM local_council_user lcu
                  WHERE lcu.subject_identifier = :localCouncilUserBaseId)
                 OR NOT :restrictToLocalCouncil) 
            """

        private const val LICENSE_FILTER =
            """
            AND ((SELECT l.license_type 
                  FROM license l
                  WHERE po.license_id = l.id)
                 IN :restrictToLicenses
                 OR po.license_id IS NULL 
                    AND :${NO_LICENCE_TYPE} IN :restrictToLicenses)
            """

        private const val FILTERS = LOCAL_COUNCIL_FILTER + LICENSE_FILTER
    }
}
