package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
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
    fun `getHasLocalAuthorityRole returns true for a standard local authority user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalAuthorityData.createLocalAuthorityUser(baseUser, isManager = false)

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLocalAuthorityUserRole = userRolesService.getHasLocalAuthorityRole(baseUser.id)

        // Assert
        Assertions.assertTrue(hasLocalAuthorityUserRole)
    }

    @Test
    fun `getHasLocalAuthorityRole returns true for a local authority manager`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalAuthorityData.createLocalAuthorityUser(baseUser, isManager = true)

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLocalAuthorityUserRole = userRolesService.getHasLocalAuthorityRole(baseUser.id)

        // Assert
        Assertions.assertTrue(hasLocalAuthorityUserRole)
    }

    @Test
    fun `getHasLocalAuthorityRole returns false for a landlord user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLandlordData.createLandlord(baseUser)
        whenever(landlordRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLocalAuthorityUserRole = userRolesService.getHasLocalAuthorityRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLocalAuthorityUserRole)
    }

    @Test
    fun `getHasLocalAuthorityRole returns false for a user without roles`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()

        // Act
        val hasLocalAuthorityUserRole = userRolesService.getHasLocalAuthorityRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLocalAuthorityUserRole)
    }

    @Test
    fun `getHasLocalAuthorityAdminRole returns true for a local authority admin`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalAuthorityData.createLocalAuthorityUser(baseUser, isManager = true)

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLocalAuthorityUserRole = userRolesService.getHasLocalAuthorityAdminRole(baseUser.id)

        // Assert
        Assertions.assertTrue(hasLocalAuthorityUserRole)
    }

    @Test
    fun `getHasLocalAuthorityAdminRole returns false for a standard local authority user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalAuthorityData.createLocalAuthorityUser(baseUser, isManager = false)

        whenever(localAuthorityUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLocalAuthorityUserRole = userRolesService.getHasLocalAuthorityAdminRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLocalAuthorityUserRole)
    }

    @Test
    fun `getHasLocalAuthorityAdminRole returns false for a landlord user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLandlordData.createLandlord(baseUser)
        whenever(landlordRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLocalAuthorityUserRole = userRolesService.getHasLocalAuthorityAdminRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLocalAuthorityUserRole)
    }

    @Test
    fun `getHasLocalAuthorityAdminRole returns false for a user without roles`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()

        // Act
        val hasLocalAuthorityUserRole = userRolesService.getHasLocalAuthorityAdminRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLocalAuthorityUserRole)
    }
}
