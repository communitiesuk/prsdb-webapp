package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser

@Suppress("ktlint:standard:function-naming")
interface OrganisationalLandlordUserRepository : JpaRepository<OrganisationalLandlordUser, Long> {
    fun findByBaseUser_Id(baseUserId: String): List<OrganisationalLandlordUser>

    @Query("SELECT u.role FROM OrganisationalLandlordUser u WHERE u.baseUser.id = :baseUserId")
    fun findRolesByBaseUserId(baseUserId: String): List<OrganisationalLandlordUserRole>

    fun findByBaseUser_IdIn(baseUserIds: Collection<String>): List<OrganisationalLandlordUser>

    fun findByOrganisationalLandlord(organisationalLandlord: OrganisationalLandlord): List<OrganisationalLandlordUser>

    fun existsByBaseUser_IdAndOrganisationalLandlord_Id(
        baseUserId: String,
        organisationalLandlordId: Long,
    ): Boolean
}
