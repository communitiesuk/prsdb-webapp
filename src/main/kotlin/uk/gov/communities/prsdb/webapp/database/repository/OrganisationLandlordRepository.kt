package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord

@Repository
@Suppress("ktlint:standard:function-naming")
interface OrganisationLandlordRepository : JpaRepository<OrganisationLandlord, Long> {
    fun findByRegistrationNumber_Number(number: Long): OrganisationLandlord?
}
