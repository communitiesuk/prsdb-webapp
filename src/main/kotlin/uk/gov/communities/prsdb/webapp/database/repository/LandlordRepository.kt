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
            LA_FILTER +
            "ORDER BY (l.phone_number || ' ' || l.email || ' ' || l.name) <->> :searchQuery",
        nativeQuery = true,
    )
    fun searchMatching(
        @Param("searchQuery") searchQuery: String,
        @Param("laUserBaseId") laUserBaseId: String,
        @Param("restrictToLA") restrictToLA: Boolean = false,
        pageable: Pageable,
    ): Page<Landlord>

    @Query(
        "SELECT l.* " +
            "FROM landlord l JOIN registration_number r on l.registration_number_id = r.id " +
            "WHERE r.number = :searchLRN " +
            LA_FILTER,
        nativeQuery = true,
    )
    fun searchMatchingLRN(
        @Param("searchLRN") searchLRN: Long,
        @Param("laUserBaseId") laUserBaseId: String,
        @Param("restrictToLA") restrictToLA: Boolean = false,
        pageable: Pageable,
    ): Page<Landlord>

    companion object {
        // Determines if the landlord has an active property ownership in the LA user's LA
        private const val LA_FILTER =
            """
             AND (EXISTS (SELECT po.id 
                          FROM property_ownership po 
                          JOIN property p ON po.property_id = p.id 
                          JOIN address a ON p.address_id = a.id
                          JOIN local_authority la ON a.local_authority_id = la.id
                          JOIN local_authority_user lau ON la.id = lau.local_authority_id
                          WHERE l.id = po.primary_landlord_id 
                          AND po.is_active 
                          AND lau.subject_identifier = :laUserBaseId)
                  OR NOT :restrictToLA) 
            """
    }
}
