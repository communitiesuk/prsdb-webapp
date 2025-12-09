package uk.gov.communities.prsdb.webapp.database.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_PROPERTIES_SEARCH
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

interface PropertyOwnershipSearchRepository {
    fun searchMatchingPRN(
        searchPRN: Long,
        localCouncilUserBaseId: String,
        restrictToLocalCouncil: Boolean = false,
        restrictToLicenses: Collection<LicensingType> = LicensingType.entries,
        pageable: Pageable,
    ): Page<PropertyOwnership>

    fun searchMatchingUPRN(
        searchUPRN: Long,
        localCouncilUserBaseId: String,
        restrictToLocalCouncil: Boolean = false,
        restrictToLicenses: Collection<LicensingType> = LicensingType.entries,
        pageable: Pageable,
    ): Page<PropertyOwnership>

    fun searchMatching(
        searchTerm: String,
        localCouncilUserBaseId: String,
        restrictToLocalCouncil: Boolean = false,
        restrictToLicenses: Collection<LicensingType> = LicensingType.entries,
        pageable: Pageable,
    ): Page<PropertyOwnership>
}

@Repository
class PropertyOwnershipSearchRepositoryImpl(
    private val entityManager: EntityManager,
) : PropertyOwnershipSearchRepository {
    override fun searchMatchingPRN(
        searchPRN: Long,
        localCouncilUserBaseId: String,
        restrictToLocalCouncil: Boolean,
        restrictToLicenses: Collection<LicensingType>,
        pageable: Pageable,
    ): Page<PropertyOwnership> {
        val query =
            """
            SELECT po.* 
            FROM property_ownership po 
            JOIN registration_number r ON po.registration_number_id = r.id 
            WHERE po.is_active AND r.number = :searchTerm
            $FILTERS
            $PAGINATION;
            """

        return entityManager.getResultPage(
            query,
            searchPRN,
            localCouncilUserBaseId,
            restrictToLocalCouncil,
            restrictToLicenses,
            pageable,
        )
    }

    override fun searchMatchingUPRN(
        searchUPRN: Long,
        localCouncilUserBaseId: String,
        restrictToLocalCouncil: Boolean,
        restrictToLicenses: Collection<LicensingType>,
        pageable: Pageable,
    ): Page<PropertyOwnership> {
        val query =
            """
            SELECT po.* 
            FROM property_ownership po 
            JOIN address a ON po.address_id = a.id 
            WHERE po.is_active AND a.uprn = :searchTerm
            $FILTERS
            $PAGINATION;
            """

        return entityManager.getResultPage(
            query,
            searchUPRN,
            localCouncilUserBaseId,
            restrictToLocalCouncil,
            restrictToLicenses,
            pageable,
        )
    }

    // We have two partial indexes on the single_line_address column:
    // - GIN (where is_active) (faster for small result sets)
    // - GIST (where is_active_duplicate_for_gist_index) (faster for large result sets)
    // Therefore, we count the number of matches, then use the result to decide which index to use (via filtering by an 'is active' column).
    override fun searchMatching(
        searchTerm: String,
        localCouncilUserBaseId: String,
        restrictToLocalCouncil: Boolean,
        restrictToLicenses: Collection<LicensingType>,
        pageable: Pageable,
    ): Page<PropertyOwnership> {
        val countQueryResult = countMatching(searchTerm, localCouncilUserBaseId, restrictToLocalCouncil, restrictToLicenses)

        val isActiveFilter =
            if (countQueryResult == MAX_ENTRIES_IN_PROPERTIES_SEARCH) {
                "po.is_active_duplicate_for_gist_index"
            } else {
                "po.is_active"
            }

        val query =
            """
            SELECT po.* 
            FROM property_ownership po 
            WHERE po.single_line_address %>> :searchTerm 
            AND $isActiveFilter
            $FILTERS
            ORDER BY po.single_line_address <->>> :searchTerm
            $PAGINATION;
            """

        return entityManager.getResultPage(
            query,
            searchTerm,
            localCouncilUserBaseId,
            restrictToLocalCouncil,
            restrictToLicenses,
            pageable,
            countQueryResult,
        )
    }

    private fun countMatching(
        searchTerm: String,
        localCouncilUserBaseId: String,
        restrictToLocalCouncil: Boolean,
        restrictToLicenses: Collection<LicensingType>,
    ): Int {
        val query =
            """
            SELECT count(*) 
            FROM (SELECT 1 
                  FROM property_ownership po 
                  WHERE po.single_line_address %>> :searchTerm 
                  AND  po.is_active 
                  $FILTERS
                  LIMIT $MAX_ENTRIES_IN_PROPERTIES_SEARCH) subquery;
            """

        return entityManager
            .createNativeQuery(query, Int::class.java)
            .setFilterParameters(searchTerm, localCouncilUserBaseId, restrictToLocalCouncil, restrictToLicenses)
            .singleResult as Int
    }

    companion object {
        // Determines whether the property's address is in the LC user's LC
        private const val LOCAL_COUNCIL_FILTER =
            """
            AND (po.local_council_id 
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
                    AND :noLicenceType IN :restrictToLicenses)
            """

        private const val FILTERS = LOCAL_COUNCIL_FILTER + LICENSE_FILTER

        private const val PAGINATION = "LIMIT :limit OFFSET :offset"

        private fun Query.setFilterParameters(
            searchTerm: Any,
            localCouncilUserBaseId: String,
            restrictToLocalCouncil: Boolean,
            restrictToLicenses: Collection<LicensingType>,
        ): Query =
            this
                .setParameter("searchTerm", searchTerm)
                .setParameter("localCouncilUserBaseId", localCouncilUserBaseId)
                .setParameter("restrictToLocalCouncil", restrictToLocalCouncil)
                .setParameter("restrictToLicenses", restrictToLicenses)
                .setParameter("noLicenceType", LicensingType.NO_LICENSING)

        @Suppress("Unchecked_Cast")
        private fun EntityManager.getResultPage(
            query: String,
            searchTerm: Any,
            localCouncilUserBaseId: String,
            restrictToLocalCouncil: Boolean,
            restrictToLicenses: Collection<LicensingType>,
            pageable: Pageable,
            countQueryResult: Int? = null,
        ): Page<PropertyOwnership> {
            val results =
                this
                    .createNativeQuery(query, PropertyOwnership::class.java)
                    .setFilterParameters(searchTerm, localCouncilUserBaseId, restrictToLocalCouncil, restrictToLicenses)
                    .setParameter("limit", pageable.pageSize)
                    .setParameter("offset", pageable.offset)
                    .resultList as List<PropertyOwnership>

            val total = countQueryResult ?: results.size

            return PageImpl(results, pageable, total.toLong())
        }
    }
}
