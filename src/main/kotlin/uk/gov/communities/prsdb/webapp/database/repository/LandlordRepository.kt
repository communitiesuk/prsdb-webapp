package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.communities.prsdb.webapp.database.entity.Landlord

// The underscore tells JPA to access fields relating to the referenced table
@Suppress("ktlint:standard:function-naming")
interface LandlordRepository : JpaRepository<Landlord?, Long?> {
    fun findByRegistrationNumber_Number(registrationNumber: Long): Landlord?

    fun findByBaseUser_Id(subjectId: String): Landlord?

    @Query(
        "SELECT l.* " +
            "FROM landlord l  " +
            "WHERE (l.phone_number || ' ' || l.email || ' ' || l.name) %> :searchQuery " +
            "ORDER BY (l.phone_number || ' ' || l.email || ' ' || l.name) <->> :searchQuery",
        nativeQuery = true,
    )
    fun searchMatching(
        @Param("searchQuery")searchQuery: String,
        pageable: Pageable,
    ): Page<Landlord>
}
