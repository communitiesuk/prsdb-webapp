package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.Landlord

interface LandlordRepository : JpaRepository<Landlord?, Long?> {
    fun findByRegistrationNumberNumber(registrationNumber: Long): Landlord?
}
