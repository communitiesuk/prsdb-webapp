package uk.gov.communities.prsdb.webapp.database.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_LANDLORDS_SEARCH
import uk.gov.communities.prsdb.webapp.models.dataModels.LandlordSearchResultDataModel

// The search methods return LandlordSearchResultDataModels instead of Landlords as we need to include the property count in the results.
interface LandlordSearchRepository {
    fun searchMatchingLRN(
        searchLRN: Long,
        localCouncilUserBaseId: String,
        restrictToLocalCouncil: Boolean = false,
        pageable: Pageable,
    ): Page<LandlordSearchResultDataModel>

    fun searchMatching(
        searchTerm: String,
        localCouncilUserBaseId: String,
        restrictToLocalCouncil: Boolean = false,
        pageable: Pageable,
    ): Page<LandlordSearchResultDataModel>
}

@Repository
class LandlordSearchRepositoryImpl(
    private val entityManager: EntityManager,
) : LandlordSearchRepository {
    override fun searchMatchingLRN(
        searchLRN: Long,
        localCouncilUserBaseId: String,
        restrictToLocalCouncil: Boolean,
        pageable: Pageable,
    ): Page<LandlordSearchResultDataModel> {
        val searchQuery =
            """
            WITH resulting_landlords AS (
                SELECT l.id
                FROM landlord l
                JOIN registration_number r ON l.registration_number_id = r.id
                WHERE r.number = :searchTerm
                ${if (restrictToLocalCouncil) LOCAL_COUNCIL_FILTER else "" }
            )
            $SELECT_FROM_RESULTING_LANDLORDS;
            """
        return entityManager.getUniqueSearchResult(searchQuery, searchLRN, localCouncilUserBaseId, restrictToLocalCouncil, pageable)
    }

    // We have two indexes on functions of the searchable columns:
    // - GIN (gin_landlord_details()) (faster for unordered results)
    // - GIST (gist_landlord_details()) (faster for ordered results)
    // Therefore, we use GIN for the count query and GIST for the search one.
    override fun searchMatching(
        searchTerm: String,
        localCouncilUserBaseId: String,
        restrictToLocalCouncil: Boolean,
        pageable: Pageable,
    ): Page<LandlordSearchResultDataModel> {
        val countQuery =
            """
            SELECT count(*) 
            FROM (SELECT 1
                  FROM landlord l
                  ${if (restrictToLocalCouncil) LOCAL_COUNCIL_FILTER_JOIN else "" }
                  WHERE gin_landlord_details(l.phone_number, l.email, l.name) %> :searchTerm
                  ${if (restrictToLocalCouncil) LOCAL_COUNCIL_FILTER_GROUP_BY else "" }
                  LIMIT $MAX_ENTRIES_IN_LANDLORDS_SEARCH
                 ) subquery;
            """
        val countResult =
            entityManager.getCountResult(
                countQuery,
                searchTerm,
                localCouncilUserBaseId,
                restrictToLocalCouncil,
            )

        val searchQuery =
            """
            WITH resulting_landlords AS (
                SELECT l.id
                FROM landlord l
                ${if (restrictToLocalCouncil) LOCAL_COUNCIL_FILTER_JOIN else "" }
                WHERE gist_landlord_details(l.phone_number, l.email, l.name) %> :searchTerm
                ${if (restrictToLocalCouncil) LOCAL_COUNCIL_FILTER_GROUP_BY else "" }
                ORDER BY gist_landlord_details(l.phone_number, l.email, l.name) <->> :searchTerm
                LIMIT :limit OFFSET :offset
            )
            $SELECT_FROM_RESULTING_LANDLORDS
            ORDER BY gist_landlord_details(l.phone_number, l.email, l.name) <->> :searchTerm;
            """
        return entityManager.getSearchResults(
            searchQuery,
            searchTerm,
            localCouncilUserBaseId,
            restrictToLocalCouncil,
            pageable,
            countResult,
        )
    }

    companion object {
        // Filters results to only landlords who have active property ownerships in LC user's LC
        private const val LOCAL_COUNCIL_FILTER =
            """
            AND EXISTS (SELECT 1 
                        FROM property_ownership po 
                        JOIN local_council_user lcu 
                        ON po.local_council_id = lcu.local_council_id AND lcu.subject_identifier = :localCouncilUserBaseId
                        WHERE l.id = po.primary_landlord_id  
                        AND po.is_active)
            """

        private const val LOCAL_COUNCIL_FILTER_JOIN =
            """
            JOIN property_ownership po ON l.id = po.primary_landlord_id AND po.is_active
            JOIN local_council_user lcu ON po.local_council_id = lcu.local_council_id AND lcu.subject_identifier = :localCouncilUserBaseId
            """

        private const val LOCAL_COUNCIL_FILTER_GROUP_BY = "GROUP BY l.id"

        private const val SELECT_FROM_RESULTING_LANDLORDS =
            """
            SELECT l.id, l.name, l.email, l.phone_number, r.number, a.single_line_address, count(po.id) as property_count
            FROM resulting_landlords rl
            JOIN landlord l ON rl.id = l.id
            JOIN registration_number r ON l.registration_number_id = r.id
            JOIN address a ON l.address_id = a.id
            LEFT JOIN property_ownership po ON l.id = po.primary_landlord_id AND po.is_active
            GROUP BY l.id, l.name, l.email, l.phone_number, r.number, a.single_line_address
            """

        private fun Query.setFilterParameters(
            searchTerm: Any,
            localCouncilUserBaseId: String,
            restrictToLocalCouncil: Boolean,
        ): Query =
            this
                .setParameter("searchTerm", searchTerm)
                .apply { if (restrictToLocalCouncil) this.setParameter("localCouncilUserBaseId", localCouncilUserBaseId) }

        private fun EntityManager.getCountResult(
            query: String,
            searchTerm: Any,
            localCouncilUserBaseId: String,
            restrictToLocalCouncil: Boolean,
        ): Long =
            this
                .createNativeQuery(query, Long::class.java)
                .setFilterParameters(searchTerm, localCouncilUserBaseId, restrictToLocalCouncil)
                .singleResult as Long

        @Suppress("Unchecked_Cast")
        private fun EntityManager.getUniqueSearchResult(
            query: String,
            searchTerm: Any,
            localCouncilUserBaseId: String,
            restrictToLocalCouncil: Boolean,
            pageable: Pageable,
        ): Page<LandlordSearchResultDataModel> {
            val searchResult =
                this
                    .createNativeQuery(query, LandlordSearchResultDataModel::class.java)
                    .setFilterParameters(searchTerm, localCouncilUserBaseId, restrictToLocalCouncil)
                    .resultList as List<LandlordSearchResultDataModel>

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
            pageable: Pageable,
            countResult: Long,
        ): Page<LandlordSearchResultDataModel> {
            val searchResult =
                this
                    .createNativeQuery(query, LandlordSearchResultDataModel::class.java)
                    .setFilterParameters(searchTerm, localCouncilUserBaseId, restrictToLocalCouncil)
                    .setParameter("limit", pageable.pageSize)
                    .setParameter("offset", pageable.offset)
                    .resultList as List<LandlordSearchResultDataModel>

            return PageImpl(searchResult, pageable, countResult)
        }
    }
}
