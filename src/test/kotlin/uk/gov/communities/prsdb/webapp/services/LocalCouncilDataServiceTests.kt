package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_INVITATIONS_CANCELLED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_INVITATION_ENTITY_TYPE
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_USERS_DELETED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_USERS_INVITED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilUser
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilUserOrInvitation
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilUserOrInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalCouncilAdminUserOrInvitationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalCouncilUserDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalCouncilUserOrInvitationDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.LocalCouncilUserAccessLevelRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilUserDeletionEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilUserDeletionInformAdminEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilUserInvitationInformAdminEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.DEFAULT_LOCAL_COUNCIL_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.DEFAULT_LOCAL_COUNCIL_USER_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createLocalCouncil
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createLocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createLocalCouncilUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockOneLoginUserData.Companion.createOneLoginUser
import java.net.URI
import java.util.Optional
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class LocalCouncilDataServiceTests {
    @Mock
    private lateinit var localCouncilUserRepository: LocalCouncilUserRepository

    @Mock
    private lateinit var localCouncilUserOrInvitationRepository: LocalCouncilUserOrInvitationRepository

    @Mock
    private lateinit var invitationService: LocalCouncilInvitationService

    @Mock
    private lateinit var oneLoginUserService: OneLoginUserService

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var registrationConfirmationSender: EmailNotificationService<LocalCouncilRegistrationConfirmationEmail>

    @Mock
    private lateinit var deletionConfirmationSender: EmailNotificationService<LocalCouncilUserDeletionEmail>

    @Mock
    private lateinit var deletionConfirmationSenderAdmin: EmailNotificationService<LocalCouncilUserDeletionInformAdminEmail>

    @Mock
    private lateinit var invitationConfirmationSenderAdmin: EmailNotificationService<LocalCouncilUserInvitationInformAdminEmail>

    @InjectMocks
    private lateinit var localCouncilDataService: LocalCouncilDataService

    @BeforeEach
    fun setup() {
        // Construct the service under test with all mocked dependencies so Mockito mocks are used at runtime.
        localCouncilDataService =
            LocalCouncilDataService(
                localCouncilUserRepository,
                localCouncilUserOrInvitationRepository,
                invitationService,
                oneLoginUserService,
                mockHttpSession,
                absoluteUrlProvider,
                registrationConfirmationSender,
                deletionConfirmationSender,
                deletionConfirmationSenderAdmin,
                invitationConfirmationSenderAdmin,
            )
    }

    @Test
    fun `getUserAndLocalCouncilIfAuthorizedUser returns the user and local council if the baseUser is authorized to access it`() {
        // Arrange
        val baseUser = createOneLoginUser()
        val localCouncil = createLocalCouncil()
        val localCouncilUser = createLocalCouncilUser(baseUser, localCouncil)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(localCouncilUser)

        // Act
        val (returnedUserModel, returnedLocalCouncil) =
            localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(
                DEFAULT_LOCAL_COUNCIL_ID,
                baseUser.id,
            )

        // Assert
        Assertions.assertEquals(
            LocalCouncilUserDataModel(
                localCouncilUser.id,
                localCouncilUser.name,
                localCouncil.name,
                localCouncilUser.isManager,
                localCouncilUser.email,
            ),
            returnedUserModel,
        )
        Assertions.assertEquals(localCouncil, returnedLocalCouncil)
    }

    @Test
    fun `getUserAndLocalCouncilIfAuthorizedUser throws an AccessDeniedException if the user is not an LA user`() {
        // Arrange
        whenever(localCouncilUserRepository.findByBaseUser_Id(anyString()))
            .thenThrow(AccessDeniedException(""))

        // Act and Assert
        assertThrows<AccessDeniedException> {
            localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(
                DEFAULT_LOCAL_COUNCIL_ID,
                createOneLoginUser().id,
            )
        }
    }

    @Test
    fun `getUserAndLocalCouncilIfAuthorizedUser throws an AccessDeniedException if the user's LA is not the given LA'`() {
        // Arrange
        val baseUser = createOneLoginUser()
        val localCouncil = createLocalCouncil()
        val localCouncilUser = createLocalCouncilUser(baseUser, localCouncil)
        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id)).thenReturn(localCouncilUser)

        // Act and Assert
        assertThrows<AccessDeniedException> {
            localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(
                DEFAULT_LOCAL_COUNCIL_ID - 1,
                baseUser.id,
            )
        }
    }

    @Test
    fun `getLocalCouncilUserIfAuthorizedLocalCouncil returns the Local Council user if they are a member of the LA`() {
        // Arrange
        val localCouncil = createLocalCouncil()
        val baseUser = createOneLoginUser()
        val localCouncilUser = createLocalCouncilUser(baseUser, localCouncil)
        whenever(localCouncilUserRepository.findById(DEFAULT_LOCAL_COUNCIL_USER_ID)).thenReturn(Optional.of(localCouncilUser))

        // Act
        val returnedLocalCouncilUser =
            localCouncilDataService.getLocalCouncilUserIfAuthorizedLocalCouncil(DEFAULT_LOCAL_COUNCIL_USER_ID, DEFAULT_LOCAL_COUNCIL_ID)

        // Assert
        Assertions.assertEquals(localCouncilUser, returnedLocalCouncilUser)
    }

    @Test
    fun `getLocalCouncilUserIfAuthorizedLocalCouncil throws a NOT_FOUND error if the local council user does not exist`() {
        // Arrange
        whenever(localCouncilUserRepository.findById(anyLong())).thenReturn(Optional.empty())

        // Act and Assert
        val errorThrown =
            assertThrows<ResponseStatusException> {
                localCouncilDataService.getLocalCouncilUserIfAuthorizedLocalCouncil(
                    DEFAULT_LOCAL_COUNCIL_USER_ID,
                    DEFAULT_LOCAL_COUNCIL_ID,
                )
            }
        assertEquals(HttpStatus.NOT_FOUND, errorThrown.statusCode)
    }

    @Test
    fun `getLocalCouncilUserIfAuthorizedLocalCouncil throws an AccessDeniedException if the LA user belongs to a different LA`() {
        // Arrange
        val localCouncil = createLocalCouncil()
        val baseUser = createOneLoginUser()
        val localCouncilUser = createLocalCouncilUser(baseUser, localCouncil)

        whenever(localCouncilUserRepository.findById(DEFAULT_LOCAL_COUNCIL_USER_ID)).thenReturn(Optional.of(localCouncilUser))

        // Act and Assert
        assertThrows<AccessDeniedException> {
            localCouncilDataService.getLocalCouncilUserIfAuthorizedLocalCouncil(
                DEFAULT_LOCAL_COUNCIL_USER_ID,
                DEFAULT_LOCAL_COUNCIL_ID + 1,
            )
        }
    }

    @Test
    fun `getUserList returns LocalCouncilUserDataModels from the LocalCouncilUserOrInvitationRepository`() {
        // Arrange
        val localCouncil = createLocalCouncil()
        val pageRequest =
            PageRequest.of(
                1,
                10,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        val user1 = LocalCouncilUserOrInvitation(1, LOCAL_COUNCIL_USER_ENTITY_TYPE, "User 1", true, localCouncil)
        val user2 = LocalCouncilUserOrInvitation(2, LOCAL_COUNCIL_USER_ENTITY_TYPE, "User 2", false, localCouncil)
        val invitation =
            LocalCouncilUserOrInvitation(3, LOCAL_COUNCIL_INVITATION_ENTITY_TYPE, "invite@test.com", false, localCouncil)

        whenever(localCouncilUserOrInvitationRepository.findByLocalCouncil(localCouncil, pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, invitation), pageRequest, 3))

        val expectedLaUserList =
            listOf(
                LocalCouncilUserOrInvitationDataModel(1, "User 1", localCouncil.name, true, false),
                LocalCouncilUserOrInvitationDataModel(2, "User 2", localCouncil.name, false, false),
                LocalCouncilUserOrInvitationDataModel(3, "invite@test.com", localCouncil.name, false, true),
            )

        // Act
        val userList =
            localCouncilDataService.getPaginatedUsersAndInvitations(
                localCouncil,
                1,
                filterOutLocalCouncilAdminInvitations = false,
            )

        // Assert
        Assertions.assertIterableEquals(expectedLaUserList, userList)
    }

    @Test
    fun `Returns all users if there are fewer users in the database than MAX_ENTRIES_IN_TABLE_PAGE`() {
        // Arrange
        val localCouncil = createLocalCouncil(123)
        val pageRequest =
            PageRequest.of(
                1,
                10,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        val user1 = LocalCouncilUserOrInvitation(1, LOCAL_COUNCIL_USER_ENTITY_TYPE, "User 1", true, localCouncil)
        val user2 = LocalCouncilUserOrInvitation(2, LOCAL_COUNCIL_USER_ENTITY_TYPE, "User 2", false, localCouncil)
        val invitation =
            LocalCouncilUserOrInvitation(3, LOCAL_COUNCIL_INVITATION_ENTITY_TYPE, "invite@test.com", false, localCouncil)

        whenever(localCouncilUserOrInvitationRepository.findByLocalCouncil(localCouncil, pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, invitation), pageRequest, 3))

        // Act
        val userList =
            localCouncilDataService.getPaginatedUsersAndInvitations(
                localCouncil,
                1,
                filterOutLocalCouncilAdminInvitations = false,
            )

        // Assert
        Assertions.assertEquals(3, userList.content.size)
    }

    @Test
    fun `Returns the requested page of users if there are more users in the database than MAX_ENTRIES_IN_TABLE_PAGE`() {
        // Arrange
        val localCouncil = createLocalCouncil(123)
        val usersFromRepository = mutableListOf<LocalCouncilUserOrInvitation>()
        for (i in 1..20) {
            usersFromRepository.add(
                LocalCouncilUserOrInvitation(
                    i.toLong(),
                    LOCAL_COUNCIL_USER_ENTITY_TYPE,
                    "User $i",
                    false,
                    localCouncil,
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

        whenever(localCouncilUserOrInvitationRepository.findByLocalCouncil(localCouncil, pageRequest1))
            .thenReturn(PageImpl(usersFromRepository.subList(0, 10).toList(), pageRequest1, 3))
        whenever(localCouncilUserOrInvitationRepository.findByLocalCouncil(localCouncil, pageRequest2))
            .thenReturn(PageImpl(usersFromRepository.subList(10, 20).toList(), pageRequest2, 3))

        val expectedUserListPage1 = mutableListOf<LocalCouncilUserOrInvitationDataModel>()
        val expectedUserListPage2 = mutableListOf<LocalCouncilUserOrInvitationDataModel>()
        for (i in 1..10) {
            expectedUserListPage1.add(LocalCouncilUserOrInvitationDataModel(i.toLong(), "User $i", "name", false, false))
        }
        for (i in 11..20) {
            expectedUserListPage2.add(LocalCouncilUserOrInvitationDataModel(i.toLong(), "User $i", "name", false, false))
        }

        // Act
        val userListPage1 =
            localCouncilDataService.getPaginatedUsersAndInvitations(
                localCouncil,
                1,
                filterOutLocalCouncilAdminInvitations = false,
            )
        val userListPage2 =
            localCouncilDataService.getPaginatedUsersAndInvitations(
                localCouncil,
                2,
                filterOutLocalCouncilAdminInvitations = false,
            )

        // Assert
        Assertions.assertIterableEquals(expectedUserListPage1, userListPage1)
        Assertions.assertIterableEquals(expectedUserListPage2, userListPage2)
    }

    @Test
    fun `getPaginatedUsersAndInvitations returns all users and invitations if filterOutLaAdminInvitations is false`() {
        // Arrange
        val localCouncil = createLocalCouncil(123)
        val pageRequest =
            PageRequest.of(
                1,
                10,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        val user1 = LocalCouncilUserOrInvitation(1, LOCAL_COUNCIL_USER_ENTITY_TYPE, "User 1", true, localCouncil)
        val user2 = LocalCouncilUserOrInvitation(2, LOCAL_COUNCIL_USER_ENTITY_TYPE, "User 2", false, localCouncil)
        val invitation =
            LocalCouncilUserOrInvitation(3, LOCAL_COUNCIL_INVITATION_ENTITY_TYPE, "invite@test.com", false, localCouncil)
        val adminInvitation =
            LocalCouncilUserOrInvitation(3, LOCAL_COUNCIL_INVITATION_ENTITY_TYPE, "invite.admin@test.com", true, localCouncil)

        whenever(localCouncilUserOrInvitationRepository.findByLocalCouncil(localCouncil, pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, invitation, adminInvitation), pageRequest, 4))
        val expectedAdminInvitationDataModel =
            LocalCouncilUserOrInvitationDataModel(3, "invite.admin@test.com", localCouncil.name, true, true)

        // Act
        val userList =
            localCouncilDataService.getPaginatedUsersAndInvitations(
                localCouncil,
                1,
                filterOutLocalCouncilAdminInvitations = false,
            )

        // Assert
        Assertions.assertEquals(4, userList.content.size)
        Assertions.assertTrue(userList.contains(expectedAdminInvitationDataModel))
    }

    @Test
    fun `getPaginatedUsersAndInvitations returns users and non-admin invitations if filterOutLaAdminInvitations is true`() {
        // Arrange
        val localCouncil = createLocalCouncil(123)
        val pageRequest =
            PageRequest.of(
                1,
                10,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        val user1 = LocalCouncilUserOrInvitation(1, LOCAL_COUNCIL_USER_ENTITY_TYPE, "User 1", true, localCouncil)
        val user2 = LocalCouncilUserOrInvitation(2, "local_council_admin", "User 2", false, localCouncil)
        val nonAdminInvitation =
            LocalCouncilUserOrInvitation(3, LOCAL_COUNCIL_INVITATION_ENTITY_TYPE, "invite@test.com", false, localCouncil)

        whenever(localCouncilUserOrInvitationRepository.findByLocalCouncilNotIncludingAdminInvitations(localCouncil, pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, nonAdminInvitation), pageRequest, 3))

        // Act
        val userList =
            localCouncilDataService.getPaginatedUsersAndInvitations(
                localCouncil,
                1,
                filterOutLocalCouncilAdminInvitations = true,
            )

        // Assert
        assertEquals(3, userList.content.size)
    }

    @Test
    fun `getPaginatedAdminUsersAndInvitations returns LocalCouncilAdminUserOrInvitationDataModels from the repository`() {
        // Arrange
        val localCouncil = createLocalCouncil()
        val pageRequest =
            PageRequest.of(
                1,
                10,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("localCouncil.name"), Sort.Order.asc("name")),
            )
        val user1 = LocalCouncilUserOrInvitation(1, LOCAL_COUNCIL_USER_ENTITY_TYPE, "User 1", true, localCouncil)
        val user2 = LocalCouncilUserOrInvitation(2, LOCAL_COUNCIL_USER_ENTITY_TYPE, "User 2", true, localCouncil)
        val invitation =
            LocalCouncilUserOrInvitation(3, LOCAL_COUNCIL_INVITATION_ENTITY_TYPE, "invite@test.com", true, localCouncil)

        whenever(localCouncilUserOrInvitationRepository.findAllByIsManagerTrue(pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, invitation), pageRequest, 3))

        val expectedAdminUsersAndInvitationList =
            listOf(
                LocalCouncilAdminUserOrInvitationDataModel(1, "User 1", localCouncil.name, false),
                LocalCouncilAdminUserOrInvitationDataModel(2, "User 2", localCouncil.name, false),
                LocalCouncilAdminUserOrInvitationDataModel(3, "invite@test.com", localCouncil.name, true),
            )

        // Act
        val adminUsersAndInvitationList = localCouncilDataService.getPaginatedAdminUsersAndInvitations(1)

        // Assert
        Assertions.assertIterableEquals(expectedAdminUsersAndInvitationList, adminUsersAndInvitationList)
    }

    @Test
    fun `updateUserAccessLevel updates the user's isManager attribute if the user exists`() {
        // Arrange
        val localCouncil = createLocalCouncil()
        val baseUser = createOneLoginUser()
        val localCouncilUser = createLocalCouncilUser(baseUser, localCouncil)
        val expectedUpdatedLocalCouncilUser = createLocalCouncilUser(baseUser, localCouncil, isManager = false)
        whenever(localCouncilUserRepository.findById(DEFAULT_LOCAL_COUNCIL_USER_ID)).thenReturn(Optional.of(localCouncilUser))

        // Act
        localCouncilDataService.updateUserAccessLevel(LocalCouncilUserAccessLevelRequestModel(false), DEFAULT_LOCAL_COUNCIL_USER_ID)

        // Assert
        val localCouncilUserCaptor = argumentCaptor<LocalCouncilUser>()
        verify(localCouncilUserRepository).save(localCouncilUserCaptor.capture())
        assertTrue(ReflectionEquals(expectedUpdatedLocalCouncilUser).matches(localCouncilUserCaptor.firstValue))
    }

    @Test
    fun `updateUserAccessLevel throws a NOT_FOUND error if the LA user does not exist`() {
        // Arrange
        whenever(localCouncilUserRepository.findById(anyLong())).thenReturn(Optional.empty())

        // Act and Assert
        val errorThrown =
            assertThrows<ResponseStatusException> {
                localCouncilDataService.updateUserAccessLevel(LocalCouncilUserAccessLevelRequestModel(false), DEFAULT_LOCAL_COUNCIL_USER_ID)
            }
        Assertions.assertEquals(HttpStatus.NOT_FOUND, errorThrown.statusCode)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `registerUserAndReturnID adds a new user to local_council_user and returns the generated ID`(isManager: Boolean) {
        // Arrange
        val baseUser = createOneLoginUser()
        val localCouncil = createLocalCouncil()
        val newLocalCouncilUser = createLocalCouncilUser(baseUser, localCouncil, isManager = isManager)

        whenever(oneLoginUserService.findOrCreate1LUser(baseUser.id)).thenReturn(baseUser)
        whenever(localCouncilUserRepository.save(any())).thenReturn(newLocalCouncilUser)
        whenever(absoluteUrlProvider.buildLocalCouncilDashboardUri()).thenReturn(URI.create("http://localhost/dashboard"))

        // Act
        val localCouncilUserID =
            localCouncilDataService.registerUserAndReturnID(
                baseUser.id,
                localCouncil,
                newLocalCouncilUser.name,
                newLocalCouncilUser.email,
                newLocalCouncilUser.isManager,
                hasAcceptedPrivacyNotice = true,
            )

        // Assert
        val localCouncilUserCaptor = argumentCaptor<LocalCouncilUser>()
        verify(localCouncilUserRepository).save(localCouncilUserCaptor.capture())
        assertTrue(ReflectionEquals(newLocalCouncilUser, "id").matches(localCouncilUserCaptor.firstValue))

        assertEquals(newLocalCouncilUser.id, localCouncilUserID)
    }

    @Test
    fun `getIsLocalCouncilUser returns true when the user is a local council user`() {
        val localCouncilUser = createLocalCouncilUser(createOneLoginUser(), createLocalCouncil())
        val baseUserId = localCouncilUser.baseUser.id

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(localCouncilUser)

        assertTrue(localCouncilDataService.getIsLocalCouncilUser(baseUserId))
    }

    @Test
    fun `getIsLocalCouncilUser returns false when the user is not a local council user`() {
        val baseUserId = "not-an-la-user"

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)

        assertFalse(localCouncilDataService.getIsLocalCouncilUser(baseUserId))
    }

    @Test
    fun `getLocalCouncilUser returns a localCouncilUser if baseUserId matches an entry in the localCouncilUser table`() {
        val localCouncilUser = createLocalCouncilUser(createOneLoginUser(), createLocalCouncil())
        val baseUserId = localCouncilUser.baseUser.id

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(localCouncilUser)

        assertEquals(localCouncilUser, localCouncilDataService.getLocalCouncilUser(baseUserId))
    }

    @Test
    fun `getLocalCouncilUser throws an error if baseUserId does not match an entry in the localCouncilUser table`() {
        val baseUserId = "not-an-la-user"

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)

        assertThrows<ResponseStatusException> { localCouncilDataService.getLocalCouncilUser(baseUserId) }
    }

    @Test
    fun `deleteUser deletes the user if they exist`() {
        // Arrange
        val localCouncilUser = createLocalCouncilUser(id = DEFAULT_LOCAL_COUNCIL_USER_ID)

        // Act
        localCouncilDataService.deleteUser(localCouncilUser)

        // Assert
        verify(localCouncilUserRepository).deleteById(DEFAULT_LOCAL_COUNCIL_USER_ID)
    }

    @Test
    fun `sendNewUserAddedEmailsToAdmins sends emails to all admin users`() {
        // Arrange
        val localCouncil = createLocalCouncil(123)
        val baseUser1 = createOneLoginUser()
        val baseUser2 = createOneLoginUser()
        val admin1 = createLocalCouncilUser(baseUser1, localCouncil, isManager = true)
        val admin2 = createLocalCouncilUser(baseUser2, localCouncil, isManager = true)

        whenever(localCouncilUserRepository.findAllByLocalCouncil_IdAndIsManagerTrue(localCouncil.id))
            .thenReturn(listOf(admin1, admin2))
        whenever(absoluteUrlProvider.buildLocalCouncilDashboardUri())
            .thenReturn(URI.create("http://localhost/dashboard"))

        val invitedEmail = "invitee@test.com"

        // Act
        localCouncilDataService.sendUserInvitedEmailsToAdmins(localCouncil, invitedEmail)

        // Assert
        val emailCaptor = argumentCaptor<LocalCouncilUserInvitationInformAdminEmail>()
        val addressCaptor = argumentCaptor<String>()
        verify(invitationConfirmationSenderAdmin, times(2))
            .sendEmail(addressCaptor.capture(), emailCaptor.capture())

        val expectedAddresses = listOf(admin1.email, admin2.email)
        assertEquals(expectedAddresses.sorted(), addressCaptor.allValues.sorted())
        for (captured in emailCaptor.allValues) {
            assertEquals(localCouncil.name, captured.councilName)
            assertEquals(invitedEmail, captured.email)
            assertEquals("http://localhost/dashboard", captured.prsdURL)
        }
    }

    @Test
    fun `sendNewUserAddedEmailsToAdmins does nothing if there are no admin users`() {
        val localCouncil = createLocalCouncil(123)
        whenever(localCouncilUserRepository.findAllByLocalCouncil_IdAndIsManagerTrue(localCouncil.id)).thenReturn(emptyList())
        // The service constructs an email using the absolute URL provider; ensure it returns a non-null URI in tests.
        whenever(absoluteUrlProvider.buildLocalCouncilDashboardUri()).thenReturn(URI.create("http://localhost/dashboard"))

        // Act
        localCouncilDataService.sendUserInvitedEmailsToAdmins(localCouncil, "nobody@test.com")

        // Assert
        verify(invitationConfirmationSenderAdmin, org.mockito.kotlin.times(0)).sendEmail(any(), any())
    }

    @Test
    fun `getUsersDeletedThisSession returns a list of LocalCouncilUser from the session`() {
        // Arrange
        val deletedUsers = listOf(createLocalCouncilUser(id = 1L), createLocalCouncilUser(id = 2L))
        whenever(mockHttpSession.getAttribute(LOCAL_COUNCIL_USERS_DELETED_THIS_SESSION))
            .thenReturn(deletedUsers)

        // Act
        val returnedDeletedUsers = localCouncilDataService.getUsersDeletedThisSession()

        // Assert
        Assertions.assertEquals(deletedUsers, returnedDeletedUsers)
    }

    @Test
    fun `getUsersDeletedThisSession returns an empty list if there are no deleted users stored in the session`() {
        // Act
        val returnedDeletedUsers = localCouncilDataService.getUsersDeletedThisSession()

        // Assert
        Assertions.assertEquals(emptyList<LocalCouncilUser>(), returnedDeletedUsers)
    }

    @Test
    fun `getUserDeletedThisSessionById returns user when deleted this session`() {
        // Arrange
        val user = createLocalCouncilUser(id = 1L)
        val deletedUsers = listOf(user, createLocalCouncilUser(id = 2L))
        whenever(mockHttpSession.getAttribute(LOCAL_COUNCIL_USERS_DELETED_THIS_SESSION))
            .thenReturn(deletedUsers)

        // Act
        val deletedUser = localCouncilDataService.getUserDeletedThisSessionById(user.id)

        // Assert
        Assertions.assertEquals(user, deletedUser)
    }

    @Test
    fun `getUserDeletedThisSessionById throws NOT FOUND error if user was not deleted this session`() {
        // Arrange
        val user = createLocalCouncilUser(id = 1L)
        val deletedUsers = listOf(createLocalCouncilUser(id = 2L))
        whenever(mockHttpSession.getAttribute(LOCAL_COUNCIL_USERS_DELETED_THIS_SESSION))
            .thenReturn(deletedUsers)

        // Act and Assert
        val errorThrown =
            assertThrows<ResponseStatusException> {
                localCouncilDataService.getUserDeletedThisSessionById(
                    user.id,
                )
            }
        assertEquals(HttpStatus.NOT_FOUND, errorThrown.statusCode)
    }

    @Test
    fun `getUserDeletedThisSessionById throws INTERNAL SERVER ERROR if user still exists`() {
        // Arrange
        val user = createLocalCouncilUser(id = 1L)
        val deletedUsers = listOf(user)
        whenever(mockHttpSession.getAttribute(LOCAL_COUNCIL_USERS_DELETED_THIS_SESSION))
            .thenReturn(deletedUsers)
        whenever(localCouncilUserRepository.existsById(user.id)).thenReturn(true)

        // Act and Assert
        val errorThrown =
            assertThrows<ResponseStatusException> {
                localCouncilDataService.getUserDeletedThisSessionById(
                    user.id,
                )
            }
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorThrown.statusCode)
    }

    @Test
    fun `addDeletedUserToSession adds a LocalCouncilUser to the list of deleted users in the session`() {
        // Arrange
        val existingDeletedUser = createLocalCouncilUser(id = 1L)
        whenever(mockHttpSession.getAttribute(LOCAL_COUNCIL_USERS_DELETED_THIS_SESSION))
            .thenReturn(listOf(existingDeletedUser))

        val userBeingDeleted = createLocalCouncilUser(id = 2L)

        // Act
        localCouncilDataService.addDeletedUserToSession(userBeingDeleted)

        // Assert
        verify(mockHttpSession).setAttribute(LOCAL_COUNCIL_USERS_DELETED_THIS_SESSION, listOf(existingDeletedUser, userBeingDeleted))
    }

    @Test
    fun `getInvitationsCancelledThisSession returns a list of Invitation from the session`() {
        // Arrange
        val cancelledInvitations = listOf(createLocalCouncilInvitation(id = 1L), createLocalCouncilInvitation(id = 2L))
        whenever(mockHttpSession.getAttribute(LOCAL_COUNCIL_INVITATIONS_CANCELLED_THIS_SESSION))
            .thenReturn(cancelledInvitations)

        // Act
        val returnedCancelledInvitations = localCouncilDataService.getInvitationsCancelledThisSession()

        // Assert
        Assertions.assertEquals(cancelledInvitations, returnedCancelledInvitations)
    }

    @Test
    fun `getInvitationsCancelledThisSession returns an empty list if there are no cancelled invitations stored in the session`() {
        // Act
        val returnedCancelledInvitations = localCouncilDataService.getInvitationsCancelledThisSession()

        // Assert
        Assertions.assertEquals(emptyList<Pair<Long, String>>(), returnedCancelledInvitations)
    }

    @Test
    fun `getInvitationCancelledThisSessionById returns the invitation when cancelled this session`() {
        // Arrange
        val invitation = createLocalCouncilInvitation(id = 1L)
        val cancelledInvitations = listOf(invitation, createLocalCouncilInvitation(id = 2L))

        whenever(mockHttpSession.getAttribute(LOCAL_COUNCIL_INVITATIONS_CANCELLED_THIS_SESSION))
            .thenReturn(cancelledInvitations)

        // Act
        val cancelledInvitation = localCouncilDataService.getInvitationCancelledThisSessionById(invitation.id)

        // Assert
        Assertions.assertEquals(invitation, cancelledInvitation)
        verify(invitationService).throwErrorIfInvitationExists(invitation)
    }

    @Test
    fun `getInvitationCancelledThisSessionById throws NOT FOUND error if invitation was not cancelled this session`() {
        // Arrange
        val invitation = createLocalCouncilInvitation(id = 1L)
        val cancelledInvitations = listOf(createLocalCouncilInvitation(id = 2L))

        whenever(mockHttpSession.getAttribute(LOCAL_COUNCIL_INVITATIONS_CANCELLED_THIS_SESSION))
            .thenReturn(cancelledInvitations)

        // Act and Assert
        val errorThrown =
            assertThrows<ResponseStatusException> {
                localCouncilDataService.getInvitationCancelledThisSessionById(
                    invitation.id,
                )
            }
        assertEquals(HttpStatus.NOT_FOUND, errorThrown.statusCode)
    }

    @Test
    fun `addCancelledInvitationToSession adds an Invitation to the list of cancelled invitations in the session`() {
        // Arrange
        val existingCancelledInvitation = createLocalCouncilInvitation(id = 1L)
        whenever(mockHttpSession.getAttribute(LOCAL_COUNCIL_INVITATIONS_CANCELLED_THIS_SESSION))
            .thenReturn(listOf(existingCancelledInvitation))
        val invitationBeingCancelled = createLocalCouncilInvitation(id = 2L)

        // Act
        localCouncilDataService.addCancelledInvitationToSession(invitationBeingCancelled)

        // Assert
        verify(mockHttpSession)
            .setAttribute(
                LOCAL_COUNCIL_INVITATIONS_CANCELLED_THIS_SESSION,
                listOf(existingCancelledInvitation, invitationBeingCancelled),
            )
    }

    @Test
    fun `getLastLocalCouncilUserInvitedThisSession returns the most recently added details for that LA from the session`() {
        // Arrange
        val invitedUsers = listOf(Pair(1, "user.1@example.com"), Pair(1, "user.2@example.com"), Pair(2, "user.2@example.com"))
        whenever(mockHttpSession.getAttribute(LOCAL_COUNCIL_USERS_INVITED_THIS_SESSION))
            .thenReturn(invitedUsers)

        // Act
        val returnedLastInvitedUser = localCouncilDataService.getLastLocalCouncilUserInvitedThisSession(1)

        // Assert
        Assertions.assertEquals("user.2@example.com", returnedLastInvitedUser)
    }

    @Test
    fun `getLastLocalCouncilUserInvitedThisSession returns null if there are no invites for that local council in the session`() {
        val invitedUsers = listOf(Pair(2, "user.1@example.com"))
        whenever(mockHttpSession.getAttribute(LOCAL_COUNCIL_USERS_INVITED_THIS_SESSION))
            .thenReturn(invitedUsers)

        // Act
        val returnedLastInvitedUser = localCouncilDataService.getLastLocalCouncilUserInvitedThisSession(1)

        // Assert
        Assertions.assertNull(returnedLastInvitedUser)
    }

    @Test
    fun `addLocalCouncilUserInvitedToSession adds a localCouncilId, email pair to the list of invited users in the session`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(LOCAL_COUNCIL_USERS_INVITED_THIS_SESSION))
            .thenReturn(listOf(Pair(1, "existing.invite@example.com")))

        // Act
        localCouncilDataService.addInvitedLocalCouncilUserToSession(1, "new.invite@example.com")

        // Assert
        verify(mockHttpSession).setAttribute(
            LOCAL_COUNCIL_USERS_INVITED_THIS_SESSION,
            listOf(Pair(1, "existing.invite@example.com"), Pair(1, "new.invite@example.com")),
        )
    }

    companion object {
        const val LOCAL_COUNCIL_USER_ENTITY_TYPE: String = "local_council_user"
    }
}
