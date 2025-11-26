package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilUser
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalCouncilUserDataModel
import java.time.Instant
import java.util.UUID

class MockLocalCouncilData {
    companion object {
        const val DEFAULT_LOCAL_COUNCIL_ID = 123

        const val NON_ADMIN_LOCAL_COUNCIL_ID = 456

        fun createLocalCouncil(
            id: Int = DEFAULT_LOCAL_COUNCIL_ID,
            custodianCode: String = "custodian code",
            name: String = "name",
        ): LocalCouncil = LocalCouncil(id, name, custodianCode)

        const val DEFAULT_LOCAL_COUNCIL_USER_ID = 456L

        fun createLocalCouncilUser(
            baseUser: OneLoginUser = MockOneLoginUserData.createOneLoginUser(),
            localCouncil: LocalCouncil = createLocalCouncil(),
            id: Long = DEFAULT_LOCAL_COUNCIL_USER_ID,
            isManager: Boolean = true,
            name: String = "name",
            email: String = "email",
        ): LocalCouncilUser = LocalCouncilUser(id, baseUser, isManager, localCouncil, name, email, true)

        const val DEFAULT_LOGGED_IN_LOCAL_COUNCIL_USER_ID = 789L

        fun createdLoggedInUserModel(userId: Long = DEFAULT_LOGGED_IN_LOCAL_COUNCIL_USER_ID): LocalCouncilUserDataModel {
            val defaultLocalCouncil = createLocalCouncil()
            return LocalCouncilUserDataModel(
                id = userId,
                localCouncilName = defaultLocalCouncil.name,
                isManager = true,
                userName = "Logged In User",
                isPending = false,
                email = "loggedinuser@example.gov.uk",
            )
        }

        const val DEFAULT_LOCAL_COUNCIL_INVITATION_ID = 123L

        fun createLocalCouncilInvitation(
            id: Long = DEFAULT_LOCAL_COUNCIL_INVITATION_ID,
            token: UUID = UUID.randomUUID(),
            email: String = "invited.email@example.com",
            invitingCouncil: LocalCouncil = createLocalCouncil(DEFAULT_LOCAL_COUNCIL_ID),
            invitedAsAdmin: Boolean = false,
            createdDate: Instant = Instant.now(),
        ): LocalCouncilInvitation {
            val localCouncilInvitation =
                LocalCouncilInvitation(
                    id = id,
                    token = token,
                    email = email,
                    invitingCouncil = invitingCouncil,
                    invitedAsAdmin = invitedAsAdmin,
                )

            ReflectionTestUtils.setField(localCouncilInvitation, "createdDate", createdDate)

            return localCouncilInvitation
        }
    }
}
