package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.LandlordUser
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.repository.LandlordUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository

class UserRolesServiceTests {
    private lateinit var landlordRepository: LandlordUserRepository
    private lateinit var localAuthorityUserRepository: LocalAuthorityUserRepository
    private lateinit var userRolesService: UserRolesService

    @BeforeEach
    fun setup() {
        landlordRepository = Mockito.mock(LandlordUserRepository::class.java)
        localAuthorityUserRepository = Mockito.mock(LocalAuthorityUserRepository::class.java)
        userRolesService = UserRolesService(landlordRepository, localAuthorityUserRepository)
    }

    fun createOneLoginUser(username: String): OneLoginUser {
        val user = OneLoginUser()
        ReflectionTestUtils.setField(user, "name", username)
        ReflectionTestUtils.setField(user, "id", username.lowercase().replace(" ", "-"))
        return user
    }

    @Test
    fun `getRolesForSubjectId returns ROLE_LANDLORD for a landlord user`() {
        // Arrange
        val baseUser = createOneLoginUser("Test User 1")
        val user = LandlordUser()
        ReflectionTestUtils.setField(user, "baseUser", baseUser)
        whenever(landlordRepository.findByBaseUser_Id("test-user-1"))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getRolesForSubjectId("test-user-1")

        // Assert
        Assertions.assertEquals(1, roles.size)
        Assertions.assertEquals("ROLE_LANDLORD", roles[0])
    }

    @Test
    fun `getRolesForSubjectId returns ROLE_LA_ADMIN for a local authority manager`() {
        // Arrange
        val baseUser = createOneLoginUser("Test User 1")
        val user = LocalAuthorityUser()
        ReflectionTestUtils.setField(user, "baseUser", baseUser)
        ReflectionTestUtils.setField(user, "isManager", true)
        whenever(localAuthorityUserRepository.findByBaseUser_Id("test-user-1"))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getRolesForSubjectId("test-user-1")

        // Assert
        Assertions.assertEquals(2, roles.size)
        Assertions.assertEquals("ROLE_LA_ADMIN", roles[0])
        Assertions.assertEquals("ROLE_LA_USER", roles[1])
    }

    @Test
    fun `getRolesForSubjectId returns ROLE_LA_USER for a standard local authority user`() {
        // Arrange
        val baseUser = createOneLoginUser("Test User 1")
        val user = LocalAuthorityUser()
        ReflectionTestUtils.setField(user, "baseUser", baseUser)
        ReflectionTestUtils.setField(user, "isManager", false)
        whenever(localAuthorityUserRepository.findByBaseUser_Id("test-user-1"))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getRolesForSubjectId("test-user-1")

        // Assert
        Assertions.assertEquals(1, roles.size)
        Assertions.assertEquals("ROLE_LA_USER", roles[0])
    }
}
