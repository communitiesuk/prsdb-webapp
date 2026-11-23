package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.database.entity.Address

interface AddressRepository : JpaRepository<Address, Long> {
    fun findByIsActiveTrueAndUprn(uprn: Long): Address?

    @Query(
        SEARCH_SELECT +
            // We only keep English LA records
            "AND a.local_council_id IS NOT NULL " +
            SEARCH_ORDER_AND_LIMIT,
        nativeQuery = true,
    )
    fun searchInEnglandWithLocalCouncil(
        houseNameOrNumber: String,
        postcode: String,
    ): List<Address>

    @Query(
        SEARCH_SELECT +
            "AND a.country IN ('England', 'Wales') " +
            SEARCH_ORDER_AND_LIMIT,
        nativeQuery = true,
    )
    fun searchInEnglandAndWales(
        houseNameOrNumber: String,
        postcode: String,
    ): List<Address>

    @Query(SEARCH_SELECT + SEARCH_ORDER_AND_LIMIT, nativeQuery = true)
    fun searchInUk(
        houseNameOrNumber: String,
        postcode: String,
    ): List<Address>

    companion object {
        const val MAX_SEARCH_RESULTS = 5

        private const val SEARCH_SELECT =
            "SELECT a.* " +
                "FROM address a " +
                "WHERE a.is_active " +
                "AND a.uprn IS NOT NULL " +
                "AND replace(a.postcode, ' ', '') = upper(replace(:postcode, ' ', '')) "

        private const val SEARCH_ORDER_AND_LIMIT =
            "ORDER BY concat_ws(' ', sub_building, building_name, building_number) <->> :houseNameOrNumber " +
                "LIMIT $MAX_SEARCH_RESULTS;"
    }
}
