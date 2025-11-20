package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.ROLE_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_ADMIN
import uk.gov.communities.prsdb.webapp.constants.ROLE_LOCAL_COUNCIL_USER
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SystemOperatorRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockOneLoginUserData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSystemOperatorData

class UserRolesServiceTests {
    private lateinit var landlordRepository: LandlordRepository
    private lateinit var localCouncilUserRepository: LocalCouncilUserRepository
    private lateinit var systemOperatorRepository: SystemOperatorRepository
    private lateinit var userRolesService: UserRolesService

    @BeforeEach
    fun setup() {
        landlordRepository = Mockito.mock(LandlordRepository::class.java)
        localCouncilUserRepository = Mockito.mock(LocalCouncilUserRepository::class.java)
        systemOperatorRepository = Mockito.mock(SystemOperatorRepository::class.java)
        userRolesService = UserRolesService(landlordRepository, localCouncilUserRepository, systemOperatorRepository)
    }

    @Test
    fun `getAllRolesForSubjectId returns ROLE_LANDLORD for a landlord user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLandlordData.createLandlord(baseUser)
        whenever(landlordRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getAllRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertEquals(1, roles.size)
        Assertions.assertEquals(ROLE_LANDLORD, roles[0])
    }

    @Test
    fun `getAllRolesForSubjectId returns ROLE_LOCAL_COUNCIL_ADMIN for a local council manager`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalCouncilData.createLocalCouncilUser(baseUser, isManager = true)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getAllRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertEquals(2, roles.size)
        Assertions.assertEquals(ROLE_LOCAL_COUNCIL_ADMIN, roles[0])
        Assertions.assertEquals(ROLE_LOCAL_COUNCIL_USER, roles[1])
    }

    @Test
    fun `getAllRolesForSubjectId returns ROLE_LOCAL_COUNCIL_USER for a standard local council user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalCouncilData.createLocalCouncilUser(baseUser, isManager = false)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getAllRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertEquals(1, roles.size)
        Assertions.assertEquals(ROLE_LOCAL_COUNCIL_USER, roles[0])
    }

    @Test
    fun `getAllRolesForSubjectId returns ROLE_SYSTEM_OPERATOR for a system operator user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val systemOperator = MockSystemOperatorData.createSystemOperator(baseUser)

        whenever(systemOperatorRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(systemOperator)

        // Act
        val roles = userRolesService.getAllRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertEquals(1, roles.size)
        Assertions.assertEquals(ROLE_SYSTEM_OPERATOR, roles[0])
    }

    @Test
    fun `getLandlordRolesForSubjectId returns ROLE_LANDLORD for a landlord user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLandlordData.createLandlord(baseUser)
        whenever(landlordRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getLandlordRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertEquals(1, roles.size)
        Assertions.assertEquals(ROLE_LANDLORD, roles[0])
    }

    @Test
    fun `getLandlordRolesForSubjectId returns no roles for a local council manager`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalCouncilData.createLocalCouncilUser(baseUser, isManager = true)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getLandlordRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertTrue(roles.isEmpty())
    }

    @Test
    fun `getLandlordRolesForSubjectId returns no roles for a standard local council user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalCouncilData.createLocalCouncilUser(baseUser, isManager = false)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getLandlordRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertTrue(roles.isEmpty())
    }

    @Test
    fun `getLandlordRolesForSubjectId returns no roles for a system operator user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val systemOperator = MockSystemOperatorData.createSystemOperator(baseUser)

        whenever(systemOperatorRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(systemOperator)

        // Act
        val roles = userRolesService.getLandlordRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertTrue(roles.isEmpty())
    }

    @Test
    fun `getLocalCouncilRolesForSubjectId returns no roles for landlord user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLandlordData.createLandlord(baseUser)
        whenever(landlordRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getLocalCouncilRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertTrue(roles.isEmpty())
    }

    @Test
    fun `getLocalCouncilRolesForSubjectId returns ROLE_LOCAL_COUNCIL_ADMIN and ROLE_LOCAL_COUNCIL_USER for a local council manager`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalCouncilData.createLocalCouncilUser(baseUser, isManager = true)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getLocalCouncilRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertEquals(2, roles.size)
        Assertions.assertTrue(roles.contains(ROLE_LOCAL_COUNCIL_ADMIN))
        Assertions.assertTrue(roles.contains(ROLE_LOCAL_COUNCIL_USER))
    }

    @Test
    fun `getLocalCouncilRolesForSubjectId returns ROLE_LOCAL_COUNCIL_USER for a standard local council user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalCouncilData.createLocalCouncilUser(baseUser, isManager = false)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val roles = userRolesService.getLocalCouncilRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertEquals(1, roles.size)
        Assertions.assertTrue(roles.contains(ROLE_LOCAL_COUNCIL_USER))
    }

    @Test
    fun `getLocalCouncilRolesForSubjectId returns ROLE_SYSTEM_OPERATOR for a system operator user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val systemOperator = MockSystemOperatorData.createSystemOperator(baseUser)

        whenever(systemOperatorRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(systemOperator)

        // Act
        val roles = userRolesService.getLocalCouncilRolesForSubjectId(baseUser.id)

        // Assert
        Assertions.assertEquals(1, roles.size)
        Assertions.assertTrue(roles.contains(ROLE_SYSTEM_OPERATOR))
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
    fun `getHasLandlordUserRole returns false for a local council manager`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalCouncilData.createLocalCouncilUser(baseUser, isManager = true)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLandlordUserRole = userRolesService.getHasLandlordUserRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLandlordUserRole)
    }

    @Test
    fun `getHasLandlordUserRole returns false for a standard local council user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalCouncilData.createLocalCouncilUser(baseUser, isManager = false)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
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
    fun `getHasLocalCouncilRole returns true for a standard local council user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalCouncilData.createLocalCouncilUser(baseUser, isManager = false)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLocalCouncilUserRole = userRolesService.getHasLocalCouncilRole(baseUser.id)

        // Assert
        Assertions.assertTrue(hasLocalCouncilUserRole)
    }

    @Test
    fun `getHasLocalCouncilRole returns true for a local council manager`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalCouncilData.createLocalCouncilUser(baseUser, isManager = true)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLocalCouncilUserRole = userRolesService.getHasLocalCouncilRole(baseUser.id)

        // Assert
        Assertions.assertTrue(hasLocalCouncilUserRole)
    }

    @Test
    fun `getHasLocalCouncilRole returns false for a landlord user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLandlordData.createLandlord(baseUser)
        whenever(landlordRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLocalCouncilUserRole = userRolesService.getHasLocalCouncilRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLocalCouncilUserRole)
    }

    @Test
    fun `getHasLocalCouncilRole returns false for a user without roles`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()

        // Act
        val hasLocalCouncilUserRole = userRolesService.getHasLocalCouncilRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLocalCouncilUserRole)
    }

    @Test
    fun `getHasLocalCouncilAdminRole returns true for a local council admin`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalCouncilData.createLocalCouncilUser(baseUser, isManager = true)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLocalCouncilUserRole = userRolesService.getHasLocalCouncilAdminRole(baseUser.id)

        // Assert
        Assertions.assertTrue(hasLocalCouncilUserRole)
    }

    @Test
    fun `getHasLocalCouncilAdminRole returns false for a standard local council user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLocalCouncilData.createLocalCouncilUser(baseUser, isManager = false)

        whenever(localCouncilUserRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLocalCouncilUserRole = userRolesService.getHasLocalCouncilAdminRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLocalCouncilUserRole)
    }

    @Test
    fun `getHasLocalCouncilAdminRole returns false for a landlord user`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()
        val user = MockLandlordData.createLandlord(baseUser)
        whenever(landlordRepository.findByBaseUser_Id(baseUser.id))
            .thenReturn(user)

        // Act
        val hasLocalCouncilUserRole = userRolesService.getHasLocalCouncilAdminRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLocalCouncilUserRole)
    }

    @Test
    fun `getHasLocalCouncilAdminRole returns false for a user without roles`() {
        // Arrange
        val baseUser = MockOneLoginUserData.createOneLoginUser()

        // Act
        val hasLocalCouncilUserRole = userRolesService.getHasLocalCouncilAdminRole(baseUser.id)

        // Assert
        Assertions.assertFalse(hasLocalCouncilUserRole)
    }
}
