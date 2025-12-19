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
        val searchQuery =
            """
            SELECT po.* 
            FROM property_ownership po 
            JOIN registration_number r ON po.registration_number_id = r.id 
            WHERE po.is_active AND r.number = :searchTerm
            $FILTERS;
            """
        return entityManager.getUniqueSearchResult(
            searchQuery,
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
        val searchQuery =
            """
            SELECT po.* 
            FROM property_ownership po 
            JOIN address a ON po.address_id = a.id 
            WHERE po.is_active AND a.uprn = :searchTerm
            $FILTERS;
            """
        return entityManager.getUniqueSearchResult(
            searchQuery,
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
        val countQuery =
            """
            SELECT count(*) 
            FROM (SELECT 1 
                  FROM property_ownership po 
                  WHERE po.single_line_address %>> :searchTerm 
                  AND  po.is_active 
                  $FILTERS
                  LIMIT $MAX_ENTRIES_IN_PROPERTIES_SEARCH
                 ) subquery;
            """
        val countResult =
            entityManager.getCountResult(
                countQuery,
                searchTerm,
                localCouncilUserBaseId,
                restrictToLocalCouncil,
                restrictToLicenses,
            )

        val isActiveFilter =
            if (countResult.toInt() == MAX_ENTRIES_IN_PROPERTIES_SEARCH) {
                "po.is_active_duplicate_for_gist_index"
            } else {
                "po.is_active"
            }

        val searchQuery =
            """
            SELECT po.* 
            FROM property_ownership po 
            WHERE po.single_line_address %>> :searchTerm 
            AND $isActiveFilter
            $FILTERS
            ORDER BY po.single_line_address <->>> :searchTerm
            LIMIT :limit OFFSET :offset;
            """
        return entityManager.getSearchResults(
            searchQuery,
            searchTerm,
            localCouncilUserBaseId,
            restrictToLocalCouncil,
            restrictToLicenses,
            pageable,
            countResult,
        )
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

        private fun EntityManager.getCountResult(
            query: String,
            searchTerm: Any,
            localCouncilUserBaseId: String,
            restrictToLocalCouncil: Boolean,
            restrictToLicenses: Collection<LicensingType>,
        ): Long =
            this
                .createNativeQuery(query, Long::class.java)
                .setFilterParameters(searchTerm, localCouncilUserBaseId, restrictToLocalCouncil, restrictToLicenses)
                .singleResult as Long

        @Suppress("Unchecked_Cast")
        private fun EntityManager.getUniqueSearchResult(
            query: String,
            searchTerm: Any,
            localCouncilUserBaseId: String,
            restrictToLocalCouncil: Boolean,
            restrictToLicenses: Collection<LicensingType>,
            pageable: Pageable,
        ): Page<PropertyOwnership> {
            val searchResult =
                this
                    .createNativeQuery(query, PropertyOwnership::class.java)
                    .setFilterParameters(searchTerm, localCouncilUserBaseId, restrictToLocalCouncil, restrictToLicenses)
                    .resultList as List<PropertyOwnership>

            // If the offset is greater than 0, any search result will be out of range
            val pagedSearchResult = if (pageable.offset > 0) emptyList() else searchResult

            return PageImpl(pagedSearchResult, pageable, searchResult.size.toLong())
        }

        @Suppress("Unchecked_Cast")
        private fun EntityManager.getSearchResults(
            query: String,
            searchTerm: Any,
            localCouncilUserBaseId: String,
            restrictToLocalCouncil: Boolean,
            restrictToLicenses: Collection<LicensingType>,
            pageable: Pageable,
            countResult: Long,
        ): Page<PropertyOwnership> {
            val searchResult =
                this
                    .createNativeQuery(query, PropertyOwnership::class.java)
                    .setFilterParameters(searchTerm, localCouncilUserBaseId, restrictToLocalCouncil, restrictToLicenses)
                    .setParameter("limit", pageable.pageSize)
                    .setParameter("offset", pageable.offset)
                    .resultList as List<PropertyOwnership>

            return PageImpl(searchResult, pageable, countResult)
        }
    }
}
