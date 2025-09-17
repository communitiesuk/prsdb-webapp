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
import org.mockito.Mock
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
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUserOrInvitation
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserOrInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityUserOrInvitationDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.LocalAuthorityUserAccessLevelRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalAuthorityUserDeletionEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalAuthorityUserDeletionInformAdminEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilUserInvitationInformAdminEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.DEFAULT_LA_USER_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthorityUser
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockOneLoginUserData.Companion.createOneLoginUser
import java.net.URI
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

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var registrationConfirmationSender: EmailNotificationService<LocalCouncilRegistrationConfirmationEmail>

    @Mock
    private lateinit var deletionConfirmationSender: EmailNotificationService<LocalAuthorityUserDeletionEmail>

    @Mock
    private lateinit var deletionConfirmationSenderAdmin: EmailNotificationService<LocalAuthorityUserDeletionInformAdminEmail>

    @Mock
    private lateinit var newLocalCouncilUserAdminEmailSender: EmailNotificationService<LocalCouncilUserInvitationInformAdminEmail>

    private lateinit var localAuthorityDataService: LocalAuthorityDataService

    @BeforeEach
    fun setup() {
        // Construct the service under test with all mocked dependencies so Mockito mocks are used at runtime.
        localAuthorityDataService =
            LocalAuthorityDataService(
                localAuthorityUserRepository,
                localAuthorityUserOrInvitationRepository,
                oneLoginUserService,
                mockHttpSession,
                absoluteUrlProvider,
                registrationConfirmationSender,
                deletionConfirmationSender,
                deletionConfirmationSenderAdmin,
                newLocalCouncilUserAdminEmailSender,
            )

        // Ensure the service uses our Mockito mocks for the email senders even if the implementation uses fields or different wiring.
        ReflectionTestUtils.setField(localAuthorityDataService, "registrationConfirmationSender", registrationConfirmationSender)
        ReflectionTestUtils.setField(localAuthorityDataService, "deletionConfirmationSender", deletionConfirmationSender)
        ReflectionTestUtils.setField(localAuthorityDataService, "deletionConfirmationSenderAdmin", deletionConfirmationSenderAdmin)
        ReflectionTestUtils.setField(localAuthorityDataService, "newLocalCouncilUserAdminEmailSender", newLocalCouncilUserAdminEmailSender)
    }

    @Test
    fun `getUserAndLocalAuthorityIfAuthorizedUser returns the user and LA when user belongs to LA`() {
        // Arrange
        val baseUser = createOneLoginUser()
        val localAuthority = createLocalAuthority()
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUser.id)).thenReturn(localAuthorityUser)

        // Act
        val (returnedUserModel, returnedLocalAuthority) =
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                localAuthority.id,
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
        whenever(localAuthorityUserRepository.findByBaseUser_Id(anyString())).thenReturn(null)

        // Act and Assert
        assertThrows<ResponseStatusException> {
            localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(
                DEFAULT_LA_ID,
                createOneLoginUser().id,
            )
        }
    }

    @Test
    fun `getUserAndLocalAuthorityIfAuthorizedUser throws an AccessDeniedException if the user's LA is not the given LA`() {
        val baseUser = createOneLoginUser()
        val localAuthority = createLocalAuthority()
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)
        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUser.id)).thenReturn(localAuthorityUser)

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
            localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(
                DEFAULT_LA_USER_ID,
                localAuthority.id,
            )

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
        Assertions.assertEquals(HttpStatus.NOT_FOUND, errorThrown.statusCode)
    }

    @Test
    fun `getLocalAuthorityUserIfAuthorizedLA throws an AccessDeniedException if the LA user belongs to a different LA`() {
        // Arrange
        val localAuthority = createLocalAuthority()
        val anotherAuthority = createLocalAuthority(999)
        val baseUser = createOneLoginUser()
        val localAuthorityUser = createLocalAuthorityUser(baseUser, anotherAuthority)
        whenever(localAuthorityUserRepository.findById(DEFAULT_LA_USER_ID)).thenReturn(Optional.of(localAuthorityUser))

        // Act and Assert
        assertThrows<AccessDeniedException> {
            localAuthorityDataService.getLocalAuthorityUserIfAuthorizedLA(DEFAULT_LA_USER_ID, localAuthority.id)
        }
    }

    @Test
    fun `getUserList returns LocalAuthorityUserDataModels from the LocalAuthorityUserOrInvitationRepository`() {
        // Arrange
        val localAuthority = createLocalAuthority()
        val pageRequest = PageRequest.of(1, 10, Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")))
        val user1 = LocalAuthorityUserOrInvitation(1, "local_authority_user", "User 1", true, localAuthority)
        val user2 = LocalAuthorityUserOrInvitation(2, "local_authority_user", "User 2", false, localAuthority)
        val invitation = LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite@test.com", false, localAuthority)

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
        Assertions.assertIterableEquals(expectedLaUserList, userList.content)
    }

    @Test
    fun `Returns all users if there are fewer users in the database than MAX_ENTRIES_IN_TABLE_PAGE`() {
        // Arrange
        val localAuthority = createLocalAuthority(123)
        val pageRequest = PageRequest.of(1, 10, Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")))
        val user1 = LocalAuthorityUserOrInvitation(1, "local_authority_user", "User 1", true, localAuthority)
        val user2 = LocalAuthorityUserOrInvitation(2, "local_authority_user", "User 2", false, localAuthority)
        val invitation = LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite@test.com", false, localAuthority)

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
        val pageRequest1 = PageRequest.of(1, 10, Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")))
        val pageRequest2 = PageRequest.of(2, 10, Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")))

        whenever(localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, pageRequest1))
            .thenReturn(PageImpl(usersFromRepository.subList(0, 10).toList(), pageRequest1, 20))
        whenever(localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, pageRequest2))
            .thenReturn(PageImpl(usersFromRepository.subList(10, 20).toList(), pageRequest2, 20))

        val expectedUserListPage1 = mutableListOf<LocalAuthorityUserOrInvitationDataModel>()
        val expectedUserListPage2 = mutableListOf<LocalAuthorityUserOrInvitationDataModel>()
        for (i in 1..10) {
            expectedUserListPage1.add(LocalAuthorityUserOrInvitationDataModel(i.toLong(), "User $i", localAuthority.name, false, false))
        }
        for (i in 11..20) {
            expectedUserListPage2.add(LocalAuthorityUserOrInvitationDataModel(i.toLong(), "User $i", localAuthority.name, false, false))
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
        Assertions.assertIterableEquals(expectedUserListPage1, userListPage1.content)
        Assertions.assertIterableEquals(expectedUserListPage2, userListPage2.content)
    }

    @Test
    fun `getPaginatedUsersAndInvitations returns all users and invitations if filterOutLaAdminInvitations is false`() {
        // Arrange
        val localAuthority = createLocalAuthority()
        val pageRequest = PageRequest.of(1, 10, Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")))
        val user1 = LocalAuthorityUserOrInvitation(1, "local_authority_user", "User 1", true, localAuthority)
        val user2 = LocalAuthorityUserOrInvitation(2, "local_authority_user", "User 2", false, localAuthority)
        val invitation = LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite@test.com", false, localAuthority)
        val adminInvitation = LocalAuthorityUserOrInvitation(4, "local_authority_invitation", "invite.admin@test.com", true, localAuthority)

        whenever(localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, invitation, adminInvitation), pageRequest, 4))

        // Act
        val userList = localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 1, filterOutLaAdminInvitations = false)

        // Assert
        Assertions.assertEquals(4, userList.content.size)
        val expectedAdminInvitationDataModel =
            LocalAuthorityUserOrInvitationDataModel(4, "invite.admin@test.com", localAuthority.name, true, true)
        Assertions.assertTrue(userList.content.contains(expectedAdminInvitationDataModel))
    }

    @Test
    fun `getPaginatedUsersAndInvitations returns users and non-admin invitations if filterOutLaAdminInvitations is true`() {
        // Arrange
        val localAuthority = createLocalAuthority()
        val pageRequest = PageRequest.of(1, 10, Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")))
        val user1 = LocalAuthorityUserOrInvitation(1, "local_authority_user", "User 1", true, localAuthority)
        val user2 = LocalAuthorityUserOrInvitation(2, "local_authority_user", "User 2", false, localAuthority)
        val nonAdminInvitation = LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite@test.com", false, localAuthority)

        whenever(localAuthorityUserOrInvitationRepository.findByLocalAuthorityNotIncludingAdminInvitations(localAuthority, pageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, nonAdminInvitation), pageRequest, 3))

        // Act
        val userList = localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 1, filterOutLaAdminInvitations = true)

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
        localAuthorityDataService.updateUserAccessLevel(LocalAuthorityUserAccessLevelRequestModel(false), DEFAULT_LA_USER_ID)

        // Assert
        val localAuthorityUserCaptor = argumentCaptor<LocalAuthorityUser>()
        verify(localAuthorityUserRepository).save(localAuthorityUserCaptor.capture())
        assertTrue(ReflectionEquals(expectedUpdatedLocalAuthorityUser).matches(localAuthorityUserCaptor.firstValue))
    }

    @Test
    fun `updateUserAccessLevel throws a NOT_FOUND error if the LA user does not exist`() {
        // Arrange
        whenever(localAuthorityUserRepository.findById(anyLong())).thenReturn(Optional.empty())

        val errorThrown =
            assertThrows<ResponseStatusException> {
                localAuthorityDataService.updateUserAccessLevel(LocalAuthorityUserAccessLevelRequestModel(false), DEFAULT_LA_USER_ID)
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
        whenever(absoluteUrlProvider.buildLocalAuthorityDashboardUri()).thenReturn(URI.create("http://localhost/dashboard"))

        // Act
        val localAuthorityUserID =
            localAuthorityDataService.registerUserAndReturnID(
                baseUser.id,
                localAuthority,
                newLocalAuthorityUser.name,
                newLocalAuthorityUser.email,
                newLocalAuthorityUser.isManager,
                hasAcceptedPrivacyNotice = true,
            )

        // Assert
        val localAuthorityUserCaptor = argumentCaptor<LocalAuthorityUser>()
        verify(localAuthorityUserRepository).save(localAuthorityUserCaptor.capture())
        assertTrue(ReflectionEquals(newLocalAuthorityUser, "id").matches(localAuthorityUserCaptor.firstValue))

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

    @Test
    fun `deleteUser deletes the user if they exist`() {
        // Arrange
        val localAuthority = createLocalAuthority()
        val baseUser = createOneLoginUser()
        val localAuthorityUser = createLocalAuthorityUser(baseUser, localAuthority)
        whenever(localAuthorityUserRepository.findById(DEFAULT_LA_USER_ID)).thenReturn(Optional.of(localAuthorityUser))

        // Act
        localAuthorityDataService.deleteUser(DEFAULT_LA_USER_ID)

        // Assert
        verify(localAuthorityUserRepository).deleteById(DEFAULT_LA_USER_ID)
    }

    @Test
    fun `deleteUser throws a NOT_FOUND error if the LA user does not exist`() {
        // Arrange
        whenever(localAuthorityUserRepository.findById(anyLong())).thenReturn(Optional.empty())

        val errorThrown = assertThrows<ResponseStatusException> { localAuthorityDataService.deleteUser(DEFAULT_LA_USER_ID) }
        Assertions.assertEquals(HttpStatus.NOT_FOUND, errorThrown.statusCode)
    }

    @Test
    fun `sendNewUserAddedEmailsToAdmins sends emails to all admin users`() {
        // Arrange
        val localAuthority = createLocalAuthority(123)
        val baseUser1 = createOneLoginUser()
        val baseUser2 = createOneLoginUser()
        val admin1 = createLocalAuthorityUser(baseUser1, localAuthority, isManager = true)
        val admin2 = createLocalAuthorityUser(baseUser2, localAuthority, isManager = true)

        whenever(localAuthorityUserRepository.findAllByLocalAuthority_IdAndIsManagerTrue(localAuthority.id))
            .thenReturn(listOf(admin1, admin2))
        whenever(absoluteUrlProvider.buildLocalAuthorityDashboardUri()).thenReturn(URI.create("http://localhost/dashboard"))

        val invitedEmail = "invitee@test.com"

        // Act
        localAuthorityDataService.sendNewUserAddedEmailsToAdmins(localAuthority, invitedEmail)

        // Assert
        val emailCaptor = argumentCaptor<LocalCouncilUserInvitationInformAdminEmail>()
        verify(newLocalCouncilUserAdminEmailSender, org.mockito.kotlin.times(2)).sendEmail(any(), emailCaptor.capture())

        // Both captured emails should have the expected fields
        for (captured in emailCaptor.allValues) {
            assertEquals(localAuthority.name, captured.councilName)
            assertEquals(invitedEmail, captured.email)
            assertEquals("http://localhost/dashboard", captured.prsdURL)
        }
    }

    @Test
    fun `sendNewUserAddedEmailsToAdmins does nothing if there are no admin users`() {
        val localAuthority = createLocalAuthority(123)
        whenever(localAuthorityUserRepository.findAllByLocalAuthority_IdAndIsManagerTrue(localAuthority.id)).thenReturn(emptyList())
        // The service constructs an email using the absolute URL provider; ensure it returns a non-null URI in tests.
        whenever(absoluteUrlProvider.buildLocalAuthorityDashboardUri()).thenReturn(URI.create("http://localhost/dashboard"))

        // Act
        localAuthorityDataService.sendNewUserAddedEmailsToAdmins(localAuthority, "nobody@test.com")

        // Assert
        verify(newLocalCouncilUserAdminEmailSender, org.mockito.kotlin.times(0)).sendEmail(any(), any())
    }
}
