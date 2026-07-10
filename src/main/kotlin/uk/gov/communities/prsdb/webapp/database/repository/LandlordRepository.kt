package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import java.time.Instant

// TODO: PDJB-1275: Update checks for landlord users to account for multiple users representing a landlord
// The underscore tells JPA to access fields relating to the referenced table
@Suppress("ktlint:standard:function-naming")
interface LandlordRepository :
    JpaRepository<IndividualLandlord?, Long?>,
    LandlordSearchRepository {
    // TODO: PDJB-1275: Update checks for landlord users to account for multiple users representing a landlord
    fun findByRegistrationNumber_Number(registrationNumber: Long): IndividualLandlord?

    // TODO: PDJB-1275: Update checks for landlord users to account for multiple users representing a landlord
    fun findByBaseUser_Id(subjectId: String): IndividualLandlord?

    fun deleteByBaseUser_Id(subjectId: String)

    fun countByCreatedDateBetween(
        start: Instant,
        end: Instant,
    ): Long

    fun countByIsVerifiedTrueAndCreatedDateBetween(
        start: Instant,
        end: Instant,
    ): Long
}
