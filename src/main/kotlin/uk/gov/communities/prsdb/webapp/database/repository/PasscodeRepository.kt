package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.Passcode

@Suppress("ktlint:standard:function-naming")
interface PasscodeRepository : JpaRepository<Passcode, String> {
    fun existsByPasscode(passcode: String): Boolean

    fun findByPasscode(passcode: String): Passcode?

    fun existsByBaseUser_Id(userId: String): Boolean
}
