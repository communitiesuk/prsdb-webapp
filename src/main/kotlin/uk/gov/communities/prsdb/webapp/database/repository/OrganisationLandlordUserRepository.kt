package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlordUser

@Suppress("ktlint:standard:function-naming")
interface OrganisationLandlordUserRepository : JpaRepository<OrganisationLandlordUser, Long> {
    fun findByBaseUser_Id(baseUserId: String): List<OrganisationLandlordUser>

    fun existsByBaseUser_IdAndOrganisationLandlord_Id(
        baseUserId: String,
        organisationLandlordId: Long,
    ): Boolean
}
