package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.communities.prsdb.webapp.database.entity.Landlord

interface LandlordRepository : JpaRepository<Landlord?, Long?> {
    // The underscore tells JPA to access fields relating to the referenced table
    @Suppress("ktlint:standard:function-naming")
    fun findByRegistrationNumber_Number(registrationNumber: Long): Landlord?

    @Query(
        "SELECT l.* " +
            "FROM landlord l  " +
            "WHERE (l.phone_number || ' ' || l.email || ' ' || l.name) %> :searchQuery " +
            "ORDER BY (l.phone_number || ' ' || l.email || ' ' || l.name) <->> :searchQuery LIMIT :limit",
        nativeQuery = true,
    )
    fun searchMatching(
        @Param("searchQuery")searchQuery: String,
        @Param("limit")limit: Int,
    ): List<Landlord>
}
