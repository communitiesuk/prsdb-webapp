package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord

@Repository
@Suppress("ktlint:standard:function-naming")
interface OrganisationLandlordRepository : JpaRepository<OrganisationalLandlord, Long> {
    fun findByRegistrationNumber_Number(number: Long): OrganisationalLandlord?
}
