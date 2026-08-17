package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import java.time.Instant

// The underscore tells JPA to access fields relating to the referenced table
@Suppress("ktlint:standard:function-naming")
interface IndividualLandlordRepository :
    JpaRepository<IndividualLandlord?, Long?>,
    LandlordSearchRepository {
    fun findByBaseUser_Id(subjectId: String): IndividualLandlord?

    fun findByBaseUser_IdIn(subjectIds: Collection<String>): List<IndividualLandlord>

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
