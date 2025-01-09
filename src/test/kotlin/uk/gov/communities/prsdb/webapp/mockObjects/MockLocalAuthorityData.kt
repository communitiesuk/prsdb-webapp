package uk.gov.communities.prsdb.webapp.mockObjects

import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import java.util.UUID

class MockLocalAuthorityData {
    companion object {
        const val DEFAULT_1L_USER_NAME = "Test user 1"

        fun get1LID(username: String) = username.lowercase().replace(" ", "-")

        fun createOneLoginUser(username: String = DEFAULT_1L_USER_NAME): OneLoginUser =
            OneLoginUser(get1LID(username), username, "$username@example.com")

        const val DEFAULT_LA_ID = 123

        fun createLocalAuthority(id: Int = DEFAULT_LA_ID): LocalAuthority = LocalAuthority(id, "name")

        const val DEFAULT_LA_USER_ID = 456L

        fun createLocalAuthorityUser(
            baseUser: OneLoginUser,
            localAuthority: LocalAuthority,
            id: Long = DEFAULT_LA_USER_ID,
            isManager: Boolean = true,
        ): LocalAuthorityUser = LocalAuthorityUser(id, baseUser, isManager, localAuthority, baseUser.name + "_LA", baseUser.email)

        const val DEFAULT_LOGGED_IN_LA_USER_ID = 789L

        fun createdLoggedInUserModel(userId: Long = DEFAULT_LOGGED_IN_LA_USER_ID): LocalAuthorityUserDataModel {
            val defaultLA = createLocalAuthority()
            return LocalAuthorityUserDataModel(
                id = userId,
                localAuthorityName = defaultLA.name,
                isManager = true,
                userName = "Logged In User",
                isPending = false,
                email = "loggedinuser@example.gov.uk",
            )
        }

        const val DEFAULT_LA_INVITATION_ID = 123L

        fun createLocalAuthorityInvitation(localAuthorityId: Int = DEFAULT_LA_ID): LocalAuthorityInvitation =
            LocalAuthorityInvitation(
                DEFAULT_LA_INVITATION_ID,
                UUID.randomUUID(),
                "invited.email@example.com",
                createLocalAuthority(localAuthorityId),
            )
    }
}
