package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationUser

@Suppress("ktlint:standard:function-naming")
interface OrganisationUserRepository : JpaRepository<OrganisationUser, Long> {
    fun findByBaseUser_Id(baseUserId: String): List<OrganisationUser>

    fun existsByBaseUser_IdAndOrganisationLandlord_Id(
        baseUserId: String,
        organisationLandlordId: Long,
    ): Boolean
}
