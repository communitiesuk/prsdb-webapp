package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import uk.gov.communities.prsdb.webapp.constants.ROLE_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LA_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SystemOperatorRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockOneLoginUserData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSystemOperatorData
import java.security.Principal

class UserRolesServiceTests {
    private lateinit var landlordRepository: LandlordRepository
    private lateinit var localAuthorityUserRepository: LocalAuthorityUserRepository
    private lateinit var systemOperatorRepository: SystemOperatorRepository
    private lateinit var userRolesService: UserRolesService

    @BeforeEach
    fun setup() {
        landlordRepository = Mockito.mock(LandlordRepository::class.java)
        localAuthorityUserRepository = Mockito.mock(LocalAuthorityUserRepository::class.java)
        systemOperatorRepository = Mockito.mock(SystemOperatorRepository::class.java)
        userRolesService = UserRolesService(landlordRepository, localAuthorityUserRepository, systemOperatorRepository)
    }

    @Test
    fun `getRolesForSubjectId returns ROLE_LANDLORD for a landlord user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLandlordData.createLandlord(baseUser)
        whenever(landlordRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertEquals(1, roles.size)
        Assertions.assertEquals(ROLE_LANDLORD, roles[0])
    }

    @Test
    fun `getRolesForSubjectId returns ROLE_LA_ADMIN for a local authority manager`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalAuthorityData.createLocalAuthorityUser(baseUser, isManager = true)

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertEquals(2, roles.size)
        Assertions.assertEquals(ROLE_LA_ADMIN, roles[0])
        Assertions.assertEquals(ROLE_LA_USER, roles[1])
    }

    @Test
    fun `getRolesForSubjectId returns ROLE_LA_USER for a standard local authority user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalAuthorityData.createLocalAuthorityUser(baseUser, isManager = false)

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertEquals(1, roles.size)
        Assertions.assertEquals(ROLE_LA_USER, roles[0])
    }

    @Test
    fun `getRolesForSubjectId returns ROLE_SYSTEM_OPERATOR for a system operator user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val systemOperator = MockSystemOperatorData.createSystemOperator(baseUser)

        whenever(systemOperatorRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(systemOperator)

        // Act
        val roles = userRolesService.getRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertEquals(1, roles.size)
        Assertions.assertEquals(ROLE_SYSTEM_OPERATOR, roles[0])
    }

    @Test
    fun `getHasLandlordUserRole returns true for a landlord user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLandlordData.createLandlord(baseUser)
        whenever(landlordRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLandlordUserRole = userRolesService.getHasLandlordUserRole(baseUser.id)

        // Assert
        Assertions.assertTrue(hasLandlordUserRole)
    }

    @Test
    fun `getHasLandlordUserRole returns false for a local authority manager`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalAuthorityData.createLocalAuthorityUser(baseUser, isManager = true)

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLandlordUserRole = userRolesService.getHasLandlordUserRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLandlordUserRole)
    }

    @Test
    fun `getHasLandlordUserRole returns false for a standard local authority user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalAuthorityData.createLocalAuthorityUser(baseUser, isManager = false)

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLandlordUserRole = userRolesService.getHasLandlordUserRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLandlordUserRole)
    }

    @Test
    fun `getHasLandlordUserRole returns false for a user without roles`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()

        // Act
        val hasLandlordUserRole = userRolesService.getHasLandlordUserRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLandlordUserRole)
    }

    @Test
    fun `getUserRolesForPrincipal returns a list of role strings`() {
        // Arrange
        val authentication = mock<Authentication>()
        whenever(authentication.authorities)
            .thenReturn(
                listOf(
                    GrantedAuthority { ROLE_LANDLORD },
                    GrantedAuthority { ROLE_LA_ADMIN },
                    GrantedAuthority { ROLE_LA_USER },
                    GrantedAuthority { ROLE_SYSTEM_OPERATOR },
                ),
            )

        val principal = authentication as Principal

        // Act
        val roles = userRolesService.getUserRolesForPrincipal(principal)

        // Assert
        Assertions.assertEquals(4, roles.size)
        Assertions.assertEquals(listOf(ROLE_LANDLORD, ROLE_LA_ADMIN, ROLE_LA_USER, ROLE_SYSTEM_OPERATOR), roles)
    }

    @Test
    fun `getUserRolesForPrincipal throws exception if principal is not an instance of Authentication`() {
        // Arrange
        val principal = mock<Principal>()

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            userRolesService.getUserRolesForPrincipal(principal)
        }
    }
}
