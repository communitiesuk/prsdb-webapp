package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser

interface OneLoginUserRepository : JpaRepository<OneLoginUser, String> {
    @Modifying
    @Query(
        "DELETE FROM one_login_user " +
            "WHERE one_login_user.id = :oneLoginUserId " +
            "AND one_login_user.id NOT IN (SELECT subject_identifier FROM local_authority_user);",
        nativeQuery = true,
    )
    fun deleteIfNotLocalAuthorityUser(oneLoginUserId: String)
}
