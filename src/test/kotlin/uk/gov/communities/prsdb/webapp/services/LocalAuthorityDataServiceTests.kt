package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
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
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_1L_USER_NAME
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_ID
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_USER_ID
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthorityUser
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.createOneLoginUser
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.get1LID
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserAccessLevelDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import java.util.Optional
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class LocalAuthorityDataServiceTests {
    @Mock
    private lateinit var localAuthorityUserRepository: LocalAuthorityUserRepository

    @Mock
    private lateinit var localAuthorityUserOrInvitationRepository: LocalAuthorityUserOrInvitationRepository

    @InjectMocks
    private lateinit var localAuthorityDataService: LocalAuthorityDataService

    @Test
    fun `getUserAndLocalAuthorityIfAuthorizedUser returns the user and local authority if the baseUser is authorized to access it`() {
        // Arrange
        val baseUser = createOneLoginUser()
        val localAuthority = createLocalAuthority()
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)
        whenever(localAuthorityUserRepository.findByBaseUser_Id(get1LID(DEFAULT_1L_USER_NAME)))
            .thenReturn(localAuthorityUser)

        // Act
        val (returnedUserModel, returnedLocalAuthority) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(DEFAULT_LA_ID, get1LID(DEFAULT_1L_USER_NAME))

        // Assert
        Assertions.assertEquals(
            LocalAuthorityUserDataModel(
                localAuthorityUser.id!!,
                baseUser.name,
                localAuthority.name,
                localAuthorityUser.isManager,
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
                get1LID(DEFAULT_1L_USER_NAME),
            )
        }
    }

    @Test
    fun `getUserAndLocalAuthorityIfAuthorizedUser throws an AccessDeniedException if the user's LA is not the given LA'`() {
        // Arrange
        val baseUser = createOneLoginUser()
        val localAuthority = createLocalAuthority()
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)
        whenever(localAuthorityUserRepository.findByBaseUser_Id(get1LID(DEFAULT_1L_USER_NAME)))
            .thenReturn(localAuthorityUser)

        // Act and Assert
        assertThrows<AccessDeniedException> {
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                DEFAULT_LA_ID - 1,
                get1LID(DEFAULT_1L_USER_NAME),
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
                baseUser.name,
                localAuthority.name,
                localAuthorityUser.isManager,
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
                LocalAuthorityUserDataModel(1, "User 1", localAuthority.name, true, false),
                LocalAuthorityUserDataModel(2, "User 2", localAuthority.name, false, false),
                LocalAuthorityUserDataModel(3, "invite@test.com", localAuthority.name, false, true),
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
        val invitation =
            LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite@test.com", false, localAuthority)
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

        val expectedUserListPage1 = mutableListOf<LocalAuthorityUserDataModel>()
        val expectedUserListPage2 = mutableListOf<LocalAuthorityUserDataModel>()
        for (i in 1..10) {
            expectedUserListPage1.add(LocalAuthorityUserDataModel(i.toLong(), "User $i", "name", false, false))
        }
        for (i in 11..20) {
            expectedUserListPage2.add(LocalAuthorityUserDataModel(i.toLong(), "User $i", "name", false, false))
        }

        // Act
        val userListPage1 = localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 1)
        val userListPage2 = localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 2)

        // Assert
        Assertions.assertIterableEquals(expectedUserListPage1, userListPage1)
        Assertions.assertIterableEquals(expectedUserListPage2, userListPage2)
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
            LocalAuthorityUserAccessLevelDataModel(false),
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
                    LocalAuthorityUserAccessLevelDataModel(false),
                    DEFAULT_LA_USER_ID,
                )
            }
        Assertions.assertEquals(HttpStatus.NOT_FOUND, errorThrown.statusCode)
    }
}
