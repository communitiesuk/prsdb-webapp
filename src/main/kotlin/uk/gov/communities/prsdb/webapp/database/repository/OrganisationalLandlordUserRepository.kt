package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser

@Suppress("ktlint:standard:function-naming")
interface OrganisationalLandlordUserRepository : JpaRepository<OrganisationalLandlordUser, Long> {
    fun findByBaseUser_Id(baseUserId: String): List<OrganisationalLandlordUser>

    fun findByOrganisationalLandlord(organisationalLandlord: OrganisationalLandlord): List<OrganisationalLandlordUser>

    fun existsByBaseUser_IdAndOrganisationalLandlord_Id(
        baseUserId: String,
        organisationalLandlordId: Long,
    ): Boolean
}
