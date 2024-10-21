package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser

interface LocalAuthorityUserRepository : JpaRepository<LocalAuthorityUser?, Long?> {
    fun findByLocalAuthority(
        localAuthority: LocalAuthority,
        pageRequest: PageRequest,
    ): List<LocalAuthorityUser>

    // The underscore tells JPA to access fields relating to the referenced table
    @Suppress("ktlint:standard:function-naming")
    fun findByBaseUser_Id(userName: String): LocalAuthorityUser?

    fun countByLocalAuthority(localAuthority: LocalAuthority): Long
}
