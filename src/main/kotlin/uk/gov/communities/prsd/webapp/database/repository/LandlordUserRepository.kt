package uk.gov.communities.prsd.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsd.webapp.database.entity.LandlordUser

interface LandlordUserRepository : JpaRepository<LandlordUser?, Long?> {
    // The underscore tells JPA to access fields relating to the referenced table
    @Suppress("ktlint:standard:function-naming")
    fun findByBaseUser_Id(userName: String): List<LandlordUser>
}
