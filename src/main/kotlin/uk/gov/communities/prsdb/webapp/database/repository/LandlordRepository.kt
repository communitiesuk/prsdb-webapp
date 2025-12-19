package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.Landlord

// The underscore tells JPA to access fields relating to the referenced table
@Suppress("ktlint:standard:function-naming")
interface LandlordRepository :
    JpaRepository<Landlord?, Long?>,
    LandlordSearchRepository {
    fun findByRegistrationNumber_Number(registrationNumber: Long): Landlord?

    fun findByBaseUser_Id(subjectId: String): Landlord?

    fun deleteByBaseUser_Id(subjectId: String)
}
