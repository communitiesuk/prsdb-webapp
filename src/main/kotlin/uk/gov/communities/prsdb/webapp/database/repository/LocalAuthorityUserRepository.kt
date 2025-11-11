package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilUser

interface LocalAuthorityUserRepository : JpaRepository<LocalCouncilUser, Long> {
    // The underscore tells JPA to access fields relating to the referenced table
    @Suppress("ktlint:standard:function-naming")
    fun findByBaseUser_Id(userName: String): LocalCouncilUser?

    @Suppress("ktlint:standard:function-naming")
    fun findAllByLocalAuthority_IdAndIsManagerTrue(localAuthorityId: Int): List<LocalCouncilUser>
}
