package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.database.entity.Address

interface AddressRepository : JpaRepository<Address, Long> {
    fun findByUprn(uprn: Long): Address?

    @Query(
        "SELECT a.* " +
            "FROM address a " +
            "WHERE a.is_active " +
            "AND a.postcode %> :postcode " +
            "AND (a.local_authority_id IS NOT NULL OR NOT :restrictToEngland) " + // We only keep English LA records
            "ORDER BY (a.postcode <->> :postcode) + (concat(a.building_name, ' ', a.building_number) <->> :houseNameOrNumber) " +
            "LIMIT $MAX_SEARCH_RESULTS;",
        nativeQuery = true,
    )
    fun search(
        houseNameOrNumber: String,
        postcode: String,
        restrictToEngland: Boolean = false,
    ): List<Address>

    companion object {
        const val MAX_SEARCH_RESULTS = 5
    }
}
