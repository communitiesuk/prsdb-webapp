package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUserOrInvitation
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserOrInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel

@ExtendWith(MockitoExtension::class)
class LocalAuthorityDataServiceTests {
    @Mock
    private lateinit var localAuthorityUserRepository: LocalAuthorityUserRepository

    @Mock
    private lateinit var localAuthorityUserOrInvitationRepository: LocalAuthorityUserOrInvitationRepository

    @InjectMocks
    private lateinit var localAuthorityDataService: LocalAuthorityDataService

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

    @Test
    fun `getLocalAuthorityForUser returns local authority if it exists for the user`() {
        // Arrange
        val localAuthority = createLocalAuthority(123)
        val baseUser = createOneLoginUser("Test user 1")
        val localAuthorityUser = createLocalAuthorityUser(baseUser, false, localAuthority)
        whenever(localAuthorityUserRepository.findByBaseUser_Id("test-user-1"))
            .thenReturn(localAuthorityUser)

        // Act
        val returnedLocalAuthority = localAuthorityDataService.getLocalAuthorityForUser("test-user-1")

        // Assert
        Assertions.assertEquals(localAuthority, returnedLocalAuthority)
    }

    @Test
    fun `getLocalAuthorityForUser returns null if user is not in a local authority`() {
        // Arrange
        whenever(localAuthorityUserRepository.findByBaseUser_Id("test-user-1"))
            .thenReturn(null)

        // Act, Assert
        Assertions.assertNull(localAuthorityDataService.getLocalAuthorityForUser("test-user-1"))
    }

    @Test
    fun `getUserList returns LocalAuthorityUserDataModels from the LocalAuthorityUserOrInvitationRepository`() {
        // Arrange
        val localAuthority = createLocalAuthority(123)
        val pageRequest =
            PageRequest.of(
                1,
                10,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        val user1 = LocalAuthorityUserOrInvitation(1, "local_authority_user", "User 1", true, localAuthority)
        val user2 = LocalAuthorityUserOrInvitation(2, "local_authority_user", "User 2", false, localAuthority)
        val invitation = LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite@test.com", false, localAuthority)
        whenever(localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, invitation), pageRequest, 3))
        val expectedLaUserList =
            listOf(
                LocalAuthorityUserDataModel("User 1", true, false),
                LocalAuthorityUserDataModel("User 2", false, false),
                LocalAuthorityUserDataModel("invite@test.com", false, true),
            )

        // Act
        val userList = localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 1)

        // Assert
        Assertions.assertIterableEquals(expectedLaUserList, userList)
    }

    @Test
    fun `Returns all users if there are fewer users in the database than MAX_ENTRIES_IN_TABLE_PAGE`() {
        // Arrange
        val localAuthority = createLocalAuthority(123)
        val pageRequest =
            PageRequest.of(
                1,
                10,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        val user1 = LocalAuthorityUserOrInvitation(1, "local_authority_user", "User 1", true, localAuthority)
        val user2 = LocalAuthorityUserOrInvitation(2, "local_authority_user", "User 2", false, localAuthority)
        val invitation = LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite@test.com", false, localAuthority)
        whenever(localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, invitation), pageRequest, 3))

        // Act
        val userList = localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 1)

        // Assert
        Assertions.assertEquals(3, userList.content.size)
    }

    @Test
    fun `Returns the requested page of users if there are more users in the database than MAX_ENTRIES_IN_TABLE_PAGE`() {
        // Arrange
        val localAuthority = createLocalAuthority(123)
        val usersFromRepository = mutableListOf<LocalAuthorityUserOrInvitation>()
        for (i in 1..20) {
            usersFromRepository.add(LocalAuthorityUserOrInvitation(i.toLong(), "local_authority_user", "User $i", false, localAuthority))
        }
        val pageRequest1 =
            PageRequest.of(
                1,
                10,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        val pageRequest2 =
            PageRequest.of(
                2,
                10,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        whenever(localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, pageRequest1))
            .thenReturn(PageImpl(usersFromRepository.subList(0, 10).toList(), pageRequest1, 3))
        whenever(localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, pageRequest2))
            .thenReturn(PageImpl(usersFromRepository.subList(10, 20).toList(), pageRequest2, 3))

        val expectedUserListPage1 = mutableListOf<LocalAuthorityUserDataModel>()
        val expectedUserListPage2 = mutableListOf<LocalAuthorityUserDataModel>()
        for (i in 1..10) {
            expectedUserListPage1.add(LocalAuthorityUserDataModel("User $i", false, false))
        }
        for (i in 11..20) {
            expectedUserListPage2.add(LocalAuthorityUserDataModel("User $i", false, false))
        }

        // Act
        val userListPage1 = localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 1)
        val userListPage2 = localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 2)

        // Assert
        Assertions.assertIterableEquals(expectedUserListPage1, userListPage1)
        Assertions.assertIterableEquals(expectedUserListPage2, userListPage2)
    }
}
