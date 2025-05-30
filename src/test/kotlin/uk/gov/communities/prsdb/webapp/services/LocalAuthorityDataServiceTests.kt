package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUserOrInvitation
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserOrInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserOrInvitationDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.LocalAuthorityUserAccessLevelRequestModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_USER_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthorityUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockOneLoginUserData.Companion.createOneLoginUser
import java.util.Optional
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class LocalAuthorityDataServiceTests {
    @Mock
    private lateinit var localAuthorityUserRepository: LocalAuthorityUserRepository

    @Mock
    private lateinit var localAuthorityUserOrInvitationRepository: LocalAuthorityUserOrInvitationRepository

    @Mock
    private lateinit var oneLoginUserService: OneLoginUserService

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @InjectMocks
    private lateinit var localAuthorityDataService: LocalAuthorityDataService

    @Test
    fun `getUserAndLocalAuthorityIfAuthorizedUser returns the user and local authority if the baseUser is authorized to access it`() {
        // Arrange
        val baseUser = createOneLoginUser()
        val localAuthority = createLocalAuthority()
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)
        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(localAuthorityUser)

        // Act
        val (returnedUserModel, returnedLocalAuthority) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                DEFAULT_LA_ID,
                baseUser.id,
            )

        // Assert
        Assertions.assertEquals(
            LocalAuthorityUserDataModel(
                localAuthorityUser.id,
                localAuthorityUser.name,
                localAuthority.name,
                localAuthorityUser.isManager,
                localAuthorityUser.email,
            ),
            returnedUserModel,
        )
        Assertions.assertEquals(localAuthority, returnedLocalAuthority)
    }

    @Test
    fun `getUserAndLocalAuthorityIfAuthorizedUser throws an AccessDeniedException if the user is not an LA user`() {
        // Arrange
        whenever(localAuthorityUserRepository.findByBaseUser_Id(anyString()))
            .thenThrow(AccessDeniedException(""))

        // Act and Assert
        assertThrows<AccessDeniedException> {
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                DEFAULT_LA_ID,
                createOneLoginUser().id,
            )
        }
    }

    @Test
    fun `getUserAndLocalAuthorityIfAuthorizedUser throws an AccessDeniedException if the user's LA is not the given LA'`() {
        // Arrange
        val baseUser = createOneLoginUser()
        val localAuthority = createLocalAuthority()
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)
        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(localAuthorityUser)

        // Act and Assert
        assertThrows<AccessDeniedException> {
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                DEFAULT_LA_ID - 1,
                baseUser.id,
            )
        }
    }

    @Test
    fun `getLocalAuthorityUserIfAuthorizedLA returns the LA user if they are a member of the LA`() {
        // Arrange
        val localAuthority = createLocalAuthority()
        val baseUser = createOneLoginUser()
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)
        val expectedLocalAuthorityUserDataModel =
            LocalAuthorityUserDataModel(
                DEFAULT_LA_USER_ID,
                localAuthorityUser.name,
                localAuthority.name,
                localAuthorityUser.isManager,
                localAuthorityUser.email,
            )

        whenever(localAuthorityUserRepository.findById(DEFAULT_LA_USER_ID)).thenReturn(Optional.of(localAuthorityUser))

        // Act
        val returnedLocalAuthorityUser =
            localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(DEFAULT_LA_USER_ID, DEFAULT_LA_ID)

        // Assert
        Assertions.assertEquals(expectedLocalAuthorityUserDataModel, returnedLocalAuthorityUser)
    }

    @Test
    fun `getLocalAuthorityUserIfAuthorizedLA throws a NOT_FOUND error if the local authority user does not exist`() {
        // Arrange
        whenever(localAuthorityUserRepository.findById(anyLong())).thenReturn(Optional.empty())

        // Act and Assert
        val errorThrown =
            assertThrows<ResponseStatusException> {
                localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(
                    DEFAULT_LA_USER_ID,
                    DEFAULT_LA_ID,
                )
            }
        assertEquals(HttpStatus.NOT_FOUND, errorThrown.statusCode)
    }

    @Test
    fun `getLocalAuthorityUserIfAuthorizedLA throws an AccessDeniedException if the LA user belongs to a different LA`() {
        // Arrange
        val localAuthority = createLocalAuthority()
        val baseUser = createOneLoginUser()
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)

        whenever(localAuthorityUserRepository.findById(DEFAULT_LA_USER_ID)).thenReturn(Optional.of(localAuthorityUser))

        // Act and Assert
        assertThrows<AccessDeniedException> {
            localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(
                DEFAULT_LA_USER_ID,
                DEFAULT_LA_ID + 1,
            )
        }
    }

    @Test
    fun `getUserList returns LocalAuthorityUserDataModels from the LocalAuthorityUserOrInvitationRepository`() {
        // Arrange
        val localAuthority = createLocalAuthority()
        val pageRequest =
            PageRequest.of(
                1,
                10,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        val user1 = LocalAuthorityUserOrInvitation(1, "local_authority_user", "User 1", true, localAuthority)
        val user2 = LocalAuthorityUserOrInvitation(2, "local_authority_user", "User 2", false, localAuthority)
        val invitation =
            LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite@test.com", false, localAuthority)
        whenever(localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, invitation), pageRequest, 3))
        val expectedLaUserList =
            listOf(
                LocalAuthorityUserOrInvitationDataModel(1, "User 1", localAuthority.name, true, false),
                LocalAuthorityUserOrInvitationDataModel(2, "User 2", localAuthority.name, false, false),
                LocalAuthorityUserOrInvitationDataModel(3, "invite@test.com", localAuthority.name, false, true),
            )

        // Act
        val userList = localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 1, filterOutLaAdminInvitations = false)

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
        val invitation =
            LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite@test.com", false, localAuthority)
        whenever(localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, invitation), pageRequest, 3))

        // Act
        val userList = localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 1, filterOutLaAdminInvitations = false)

        // Assert
        Assertions.assertEquals(3, userList.content.size)
    }

    @Test
    fun `Returns the requested page of users if there are more users in the database than MAX_ENTRIES_IN_TABLE_PAGE`() {
        // Arrange
        val localAuthority = createLocalAuthority(123)
        val usersFromRepository = mutableListOf<LocalAuthorityUserOrInvitation>()
        for (i in 1..20) {
            usersFromRepository.add(
                LocalAuthorityUserOrInvitation(
                    i.toLong(),
                    "local_authority_user",
                    "User $i",
                    false,
                    localAuthority,
                ),
            )
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

        val expectedUserListPage1 = mutableListOf<LocalAuthorityUserOrInvitationDataModel>()
        val expectedUserListPage2 = mutableListOf<LocalAuthorityUserOrInvitationDataModel>()
        for (i in 1..10) {
            expectedUserListPage1.add(LocalAuthorityUserOrInvitationDataModel(i.toLong(), "User $i", "name", false, false))
        }
        for (i in 11..20) {
            expectedUserListPage2.add(LocalAuthorityUserOrInvitationDataModel(i.toLong(), "User $i", "name", false, false))
        }

        // Act
        val userListPage1 =
            localAuthorityDataService.getPaginatedUsersAndInvitations(
                localAuthority,
                1,
                filterOutLaAdminInvitations = false,
            )
        val userListPage2 =
            localAuthorityDataService.getPaginatedUsersAndInvitations(
                localAuthority,
                2,
                filterOutLaAdminInvitations = false,
            )

        // Assert
        Assertions.assertIterableEquals(expectedUserListPage1, userListPage1)
        Assertions.assertIterableEquals(expectedUserListPage2, userListPage2)
    }

    @Test
    fun `getPaginatedUsersAndInvitations returns all users and invitations if filterOutLaAdminInvitations is false`() {
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
        val invitation =
            LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite@test.com", false, localAuthority)
        val adminInvitation =
            LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite.admin@test.com", true, localAuthority)
        whenever(localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, invitation, adminInvitation), pageRequest, 4))
        val expectedAdminInvitationDataModel =
            LocalAuthorityUserOrInvitationDataModel(3, "invite.admin@test.com", localAuthority.name, true, true)

        // Act
        val userList = localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 1, filterOutLaAdminInvitations = false)

        // Assert
        Assertions.assertEquals(4, userList.content.size)
        Assertions.assertTrue(userList.contains(expectedAdminInvitationDataModel))
    }

    @Test
    fun `getPaginatedUsersAndInvitations returns users and non-admin invitations if filterOutLaAdminInvitations is true`() {
        // Arrange
        val localAuthority = createLocalAuthority(123)
        val pageRequest =
            PageRequest.of(
                1,
                10,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        val user1 = LocalAuthorityUserOrInvitation(1, "local_authority_user", "User 1", true, localAuthority)
        val user2 = LocalAuthorityUserOrInvitation(2, "local_authority_admin", "User 2", false, localAuthority)
        val nonAdminInvitation =
            LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite@test.com", false, localAuthority)

        whenever(localAuthorityUserOrInvitationRepository.findByLocalAuthorityNotIncludingAdminInvitations(localAuthority, pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, nonAdminInvitation), pageRequest, 3))

        // Act
        val userList =
            localAuthorityDataService.getPaginatedUsersAndInvitations(
                localAuthority,
                1,
                filterOutLaAdminInvitations = true,
            )

        // Assert
        assertEquals(3, userList.content.size)
    }

    @Test
    fun `updateUserAccessLevel updates the user's isManager attribute if the user exists`() {
        // Arrange
        val localAuthority = createLocalAuthority()
        val baseUser = createOneLoginUser()
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)
        val expectedUpdatedLocalAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority, isManager = false)
        whenever(localAuthorityUserRepository.findById(DEFAULT_LA_USER_ID)).thenReturn(Optional.of(localAuthorityUser))

        // Act
        localAuthorityDataService.updateUserAccessLevel(
            LocalAuthorityUserAccessLevelRequestModel(false),
            DEFAULT_LA_USER_ID,
        )

        // Assert
        val localAuthorityUserCaptor = captor<LocalAuthorityUser>()
        verify(localAuthorityUserRepository).save(localAuthorityUserCaptor.capture())
        assertTrue(ReflectionEquals(expectedUpdatedLocalAuthorityUser).matches(localAuthorityUserCaptor.value))
    }

    @Test
    fun `updateUserAccessLevel throws a NOT_FOUND error if the LA user does not exist`() {
        // Arrange
        whenever(localAuthorityUserRepository.findById(anyLong())).thenReturn(Optional.empty())

        // Act and Assert
        val errorThrown =
            assertThrows<ResponseStatusException> {
                localAuthorityDataService.updateUserAccessLevel(
                    LocalAuthorityUserAccessLevelRequestModel(false),
                    DEFAULT_LA_USER_ID,
                )
            }
        Assertions.assertEquals(HttpStatus.NOT_FOUND, errorThrown.statusCode)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `registerUserAndReturnID adds a new user to local_authority_user and returns the generated ID`(isManager: Boolean) {
        // Arrange
        val baseUser = createOneLoginUser()
        val localAuthority = createLocalAuthority()
        val newLocalAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority, isManager = isManager)

        whenever(oneLoginUserService.findOrCreate1LUser(baseUser.id)).thenReturn(baseUser)
        whenever(localAuthorityUserRepository.save(any())).thenReturn(newLocalAuthorityUser)

        // Act
        val localAuthorityUserID =
            localAuthorityDataService.registerUserAndReturnID(
                baseUser.id,
                localAuthority,
                newLocalAuthorityUser.name,
                newLocalAuthorityUser.email,
                newLocalAuthorityUser.isManager,
            )

        // Assert
        val localAuthorityUserCaptor = captor<LocalAuthorityUser>()
        verify(localAuthorityUserRepository).save(localAuthorityUserCaptor.capture())
        assertTrue(ReflectionEquals(newLocalAuthorityUser, "id").matches(localAuthorityUserCaptor.value))

        assertEquals(newLocalAuthorityUser.id, localAuthorityUserID)
    }

    @Test
    fun `getIsLocalAuthorityUser returns true when the user is a local authority user`() {
        val localAuthorityUser = createLocalAuthorityUser(createOneLoginUser(), createLocalAuthority())
        val baseUserId = localAuthorityUser.baseUser.id

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(localAuthorityUser)

        assertTrue(localAuthorityDataService.getIsLocalAuthorityUser(baseUserId))
    }

    @Test
    fun `getIsLocalAuthorityUser returns false when the user is not a local authority user`() {
        val baseUserId = "not-an-la-user"

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)

        assertFalse(localAuthorityDataService.getIsLocalAuthorityUser(baseUserId))
    }

    @Test
    fun `getLocalAuthorityUser returns a localAuthorityUser if baseUserId matches an entry in the localAuthorityUser table`() {
        val localAuthorityUser = createLocalAuthorityUser(createOneLoginUser(), createLocalAuthority())
        val baseUserId = localAuthorityUser.baseUser.id

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(localAuthorityUser)

        assertEquals(localAuthorityUser, localAuthorityDataService.getLocalAuthorityUser(baseUserId))
    }

    @Test
    fun `getLocalAuthorityUser throws an error if baseUserId does not match an entry in the localAuthorityUser table`() {
        val baseUserId = "not-an-la-user"

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)

        assertThrows<ResponseStatusException> { localAuthorityDataService.getLocalAuthorityUser(baseUserId) }
    }
}
