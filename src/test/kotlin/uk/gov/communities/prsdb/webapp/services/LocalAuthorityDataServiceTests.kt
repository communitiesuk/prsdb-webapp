package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUserInvitation
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository

class LocalAuthorityDataServiceTests {
    private lateinit var localAuthorityUsersRepository: LocalAuthorityUserRepository
    private lateinit var localAuthorityUserInvitationRepository: LocalAuthorityUserInvitationRepository
    private lateinit var localAuthorityDataService: LocalAuthorityDataService

    @BeforeEach
    fun setup() {
        localAuthorityUsersRepository = Mockito.mock(LocalAuthorityUserRepository::class.java)
        localAuthorityUserInvitationRepository = Mockito.mock(LocalAuthorityUserInvitationRepository::class.java)
        localAuthorityDataService =
            LocalAuthorityDataService(
                localAuthorityUsersRepository,
                localAuthorityUserInvitationRepository,
            )
    }

    fun createOneLoginUser(username: String): OneLoginUser {
        val user = OneLoginUser()
        ReflectionTestUtils.setField(user, "name", username)
        ReflectionTestUtils.setField(user, "id", username.lowercase().replace(" ", "-"))
        return user
    }

    fun createLocalAuthority(id: Int): LocalAuthority {
        val localAuthority = LocalAuthority()
        ReflectionTestUtils.setField(localAuthority, "id", id)

        return localAuthority
    }

    fun createLocalAuthorityUser(
        baseUser: OneLoginUser,
        isManager: Boolean,
        localAuthority: LocalAuthority,
    ): LocalAuthorityUser {
        val user = LocalAuthorityUser()
        ReflectionTestUtils.setField(user, "baseUser", baseUser)
        ReflectionTestUtils.setField(user, "isManager", isManager)
        ReflectionTestUtils.setField(user, "localAuthority", localAuthority)

        return user
    }

    fun createLocalAuthorityUserInvitation(
        invitedEmail: String,
        localAuthority: LocalAuthority,
    ): LocalAuthorityUserInvitation {
        val invitation = LocalAuthorityUserInvitation()
        ReflectionTestUtils.setField(invitation, "invitedEmailAddress", invitedEmail)
        ReflectionTestUtils.setField(invitation, "localAuthority", localAuthority)

        return invitation
    }

    @Test
    fun `getLocalAuthorityUsersForLocalAuthority returns a populated list of LocalAuthorityUserDataModel`() {
        // Arrange
        val localAuthorityId = 123
        val localAuthorityTest = createLocalAuthority(localAuthorityId)
        val baseUser1 = createOneLoginUser("Test user 1")
        val baseUser2 = createOneLoginUser("Test user 2")
        val localAuthorityUser1 = createLocalAuthorityUser(baseUser1, true, localAuthorityTest)
        val localAuthorityUser2 = createLocalAuthorityUser(baseUser2, false, localAuthorityTest)
        Mockito
            .`when`(localAuthorityUsersRepository.findByLocalAuthorityOrderByBaseUser_Name(localAuthorityTest))
            .thenReturn(listOf(localAuthorityUser1, localAuthorityUser2))

        // Act
        val laUserList = localAuthorityDataService.getLocalAuthorityUsersForLocalAuthority(localAuthorityTest)

        // Assert
        Assertions.assertEquals(2, laUserList.size)
        Assertions.assertEquals("Test user 1", laUserList[0].userName)
        Assertions.assertEquals("Test user 2", laUserList[1].userName)
        Assertions.assertEquals(true, laUserList[0].isManager)
        Assertions.assertEquals(false, laUserList[1].isManager)
        Assertions.assertEquals(false, laUserList[0].isPending)
        Assertions.assertEquals(false, laUserList[1].isPending)
    }

    @Test
    fun `getLocalAuthorityForUser returns local authority if it exists for the user`() {
        // Arrange
        val localAuthority = createLocalAuthority(123)
        val baseUser = createOneLoginUser("Test user 1")
        val localAuthorityUser = createLocalAuthorityUser(baseUser, false, localAuthority)
        Mockito
            .`when`(localAuthorityUsersRepository.findByBaseUser_Id("test-user-1"))
            .thenReturn(localAuthorityUser)

        // Act
        val returnedLocalAuthority = localAuthorityDataService.getLocalAuthorityForUser("test-user-1")

        // Assert
        Assertions.assertEquals(localAuthority, returnedLocalAuthority)
    }

    @Test
    fun `getLocalAuthorityForUser returns null if user is not in a local authority`() {
        // Arrange
        Mockito
            .`when`(localAuthorityUsersRepository.findByBaseUser_Id("test-user-1"))
            .thenReturn(null)

        // Act, Assert
        Assertions.assertNull(localAuthorityDataService.getLocalAuthorityForUser("test-user-1"))
    }

    @Test
    fun `getLocalAuthorityPendingUsersForLocalAuthority returns a populated list of LocalAuthorityUserDataModel`() {
        // Arrange
        val localAuthorityId = 123
        val localAuthorityTest = createLocalAuthority(localAuthorityId)
        val invitation1 = createLocalAuthorityUserInvitation("invited.user@example.com", localAuthorityTest)
        val invitation2 = createLocalAuthorityUserInvitation("another.user@example.com", localAuthorityTest)
        Mockito
            .`when`(localAuthorityUserInvitationRepository.findByLocalAuthorityOrderByInvitedEmailAddress(localAuthorityTest))
            .thenReturn(listOf(invitation1, invitation2))

        // Act
        val laInvitedUsers = localAuthorityDataService.getLocalAuthorityPendingUsersForLocalAuthority(localAuthorityTest)

        // Assert
        Assertions.assertEquals(2, laInvitedUsers.size)
        Assertions.assertEquals("invited.user@example.com", laInvitedUsers[0].userName)
        Assertions.assertEquals("another.user@example.com", laInvitedUsers[1].userName)
        Assertions.assertEquals(false, laInvitedUsers[0].isManager)
        Assertions.assertEquals(false, laInvitedUsers[1].isManager)
        Assertions.assertEquals(true, laInvitedUsers[0].isPending)
        Assertions.assertEquals(true, laInvitedUsers[1].isPending)
    }
}
