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

    fun deleteByBaseUser_Id(subjectId: String)

    @Query(
        "SELECT l.* " +
            "FROM landlord l " +
            "JOIN registration_number r on l.registration_number_id = r.id " +
            "WHERE r.number = :searchLRN " +
            LOCAL_COUNCIL_FILTER,
        nativeQuery = true,
    )
    fun searchMatchingLRN(
        @Param("searchLRN") searchLRN: Long,
        @Param("localCouncilUserBaseId") localCouncilUserBaseId: String,
        @Param("restrictToLocalCouncil") restrictToLocalCouncil: Boolean = false,
        pageable: Pageable,
    ): Page<Landlord>

    @Query(
        "SELECT l.* " +
            "FROM landlord l " +
            "WHERE (l.phone_number || ' ' || l.email || ' ' || l.name) %> :searchQuery " +
            LOCAL_COUNCIL_FILTER +
            "ORDER BY (l.phone_number || ' ' || l.email || ' ' || l.name) <->> :searchQuery",
        nativeQuery = true,
    )
    fun searchMatching(
        @Param("searchQuery") searchQuery: String,
        @Param("localCouncilUserBaseId") laUserBaseId: String,
        @Param("restrictToLocalCouncil") restrictToLocalCouncil: Boolean = false,
        pageable: Pageable,
    ): Page<Landlord>

    companion object {
        // Determines if the landlord has an active property ownership in the LA user's LA
        const val LOCAL_COUNCIL_FILTER =
            """
             AND (EXISTS (SELECT po.id 
                          FROM property_ownership po 
                          JOIN address a ON po.address_id = a.id
                          JOIN local_council lc ON a.local_council_id = lc.id
                          JOIN local_council_user lcu ON lc.id = lcu.local_council_id
                          WHERE l.id = po.primary_landlord_id 
                          AND po.is_active 
                          AND lcu.subject_identifier = :localCouncilUserBaseId)
                  OR NOT :restrictToLocalCouncil) 
            """
    }
}
