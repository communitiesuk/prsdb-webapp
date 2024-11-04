package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
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
        ReflectionTestUtils.setField(localAuthority, "name", "name")
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
        val returnedLocalAuthority = localAuthorityDataService.getLocalAuthorityIfValidUser(123, "test-user-1")

        // Assert
        Assertions.assertEquals(localAuthority, returnedLocalAuthority)
    }

    // TODO: No longer applicable - throws error instead
//    @Test
//    fun `getLocalAuthorityForUser returns null if user is not in a local authority`() {
//        // Arrange
//        whenever(localAuthorityUserRepository.findByBaseUser_Id("test-user-1"))
//            .thenReturn(null)
//
//        // Act, Assert
//        Assertions.assertNull(localAuthorityDataService.getLocalAuthorityIfValidUser(123, "test-user-1"))
//    }

    @Test
    fun `getUserList returns LocalAuthorityUserDataModels from the LocalAuthorityUserOrInvitationRepository`() {
        val localAuthority = createLocalAuthority(123)
        val expectedPageRequest =
            PageRequest.of(
                1,
                10,
                Sort.by(Sort.Order.desc("entityType"), Sort.Order.asc("name")),
            )
        val user1 = LocalAuthorityUserOrInvitation(1, "local_authority_user", "User 1", true, localAuthority)
        val user2 = LocalAuthorityUserOrInvitation(2, "local_authority_user", "User 2", false, localAuthority)
        val invitation =
            LocalAuthorityUserOrInvitation(3, "local_authority_invitation", "invite@test.com", false, localAuthority)
        Mockito
            .`when`(localAuthorityUserOrInvitationRepository.findByLocalAuthority(localAuthority, expectedPageRequest))
            .thenReturn(PageImpl(listOf(user1, user2, invitation), expectedPageRequest, 3))

        val userList = localAuthorityDataService.getPaginatedUsersAndInvitations(localAuthority, 1)

        val expectedLaUserList =
            listOf(
                LocalAuthorityUserDataModel("User 1", localAuthority.name, true, false),
                LocalAuthorityUserDataModel("User 2", localAuthority.name, false, false),
                LocalAuthorityUserDataModel("invite@test.com", localAuthority.name, false, true),
            )
        Assertions.assertIterableEquals(expectedLaUserList, userList)
    }
}
