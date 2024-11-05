package uk.gov.communities.prsdb.webapp.mockObjects

import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser

class MockLocalAuthorityData {
    companion object {
        const val DEFAULT_1L_USER_NAME = "Test user 1"

        fun get1LID(username: String) = username.lowercase().replace(" ", "-")

        fun createOneLoginUser(username: String = DEFAULT_1L_USER_NAME): OneLoginUser {
            val user = OneLoginUser()
            ReflectionTestUtils.setField(user, "id", get1LID(username))
            ReflectionTestUtils.setField(user, "name", username)
            return user
        }

        const val DEFAULT_1L_ID = 123

        fun createLocalAuthority(id: Int = DEFAULT_1L_ID): LocalAuthority {
            val localAuthority = LocalAuthority()
            ReflectionTestUtils.setField(localAuthority, "id", id)
            ReflectionTestUtils.setField(localAuthority, "name", "name")
            return localAuthority
        }

        const val DEFAULT_LA_USER_ID = 456L

        fun createLocalAuthorityUser(
            baseUser: OneLoginUser,
            localAuthority: LocalAuthority,
            id: Long = DEFAULT_LA_USER_ID,
            isManager: Boolean = true,
        ): LocalAuthorityUser {
            val user = LocalAuthorityUser()
            ReflectionTestUtils.setField(user, "baseUser", baseUser)
            ReflectionTestUtils.setField(user, "localAuthority", localAuthority)
            ReflectionTestUtils.setField(user, "id", id)
            ReflectionTestUtils.setField(user, "isManager", isManager)
            return user
        }
    }
}
