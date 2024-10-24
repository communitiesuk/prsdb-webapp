package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel

class LocalAuthorityDataServiceTests {
    private lateinit var localAuthorityUsersRepository: LocalAuthorityUserRepository
    private lateinit var localAuthorityInvitationRepository: LocalAuthorityInvitationRepository
    private lateinit var localAuthorityDataService: LocalAuthorityDataService

    @BeforeEach
    fun setup() {
        localAuthorityUsersRepository = Mockito.mock(LocalAuthorityUserRepository::class.java)
        localAuthorityInvitationRepository = Mockito.mock(LocalAuthorityInvitationRepository::class.java)
        localAuthorityDataService =
            LocalAuthorityDataService(
                localAuthorityUsersRepository,
                localAuthorityInvitationRepository,
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
    ): LocalAuthorityInvitation {
        val invitation = LocalAuthorityInvitation()
        ReflectionTestUtils.setField(invitation, "invitedEmail", invitedEmail)
        ReflectionTestUtils.setField(invitation, "invitingAuthority", localAuthority)

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
        val pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "baseUser_name"))
        Mockito
            .`when`(localAuthorityUsersRepository.findByLocalAuthority(localAuthorityTest, pageRequest))
            .thenReturn(listOf(localAuthorityUser1, localAuthorityUser2))
        val expectedLaUserList =
            listOf(
                LocalAuthorityUserDataModel("Test user 1", true, false),
                LocalAuthorityUserDataModel("Test user 2", false, false),
            )

        // Act
        val laUserList = localAuthorityDataService.getLocalAuthorityUsersForLocalAuthority(localAuthorityTest, pageRequest)

        // Assert
        Assertions.assertEquals(2, laUserList.size)
        Assertions.assertEquals(expectedLaUserList, laUserList)
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
        val pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "invitedEmail"))
        Mockito
            .`when`(localAuthorityInvitationRepository.findByInvitingAuthority(localAuthorityTest, pageRequest))
            .thenReturn(listOf(invitation1, invitation2))
        val expectedUserList =
            listOf(
                LocalAuthorityUserDataModel("invited.user@example.com", false, true),
                LocalAuthorityUserDataModel("another.user@example.com", false, true),
            )

        // Act
        val laInvitedUsers = localAuthorityDataService.getLocalAuthorityPendingUsersForLocalAuthority(localAuthorityTest, pageRequest)

        // Assert
        Assertions.assertEquals(2, laInvitedUsers.size)
        Assertions.assertEquals(expectedUserList, laInvitedUsers)
    }
}
