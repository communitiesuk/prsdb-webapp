package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser

interface LocalAuthorityUserRepository : JpaRepository<LocalAuthorityUser?, Long?> {
    // The underscore tells JPA to access fields relating to the referenced table
    @Suppress("ktlint:standard:function-naming")
    fun findByLocalAuthorityOrderByBaseUser_Name(localAuthority: LocalAuthority): List<LocalAuthorityUser>

    @Suppress("ktlint:standard:function-naming")
    fun findByBaseUser_Id(userName: String): LocalAuthorityUser?
}
