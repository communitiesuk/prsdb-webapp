package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData
import uk.gov.communities.prsdb.webapp.mockObjects.MockOneLoginUserData

class UserRolesServiceTests {
    private lateinit var landlordRepository: LandlordRepository
    private lateinit var localAuthorityUserRepository: LocalAuthorityUserRepository
    private lateinit var userRolesService: UserRolesService

    @BeforeEach
    fun setup() {
        landlordRepository = Mockito.mock(LandlordRepository::class.java)
        localAuthorityUserRepository = Mockito.mock(LocalAuthorityUserRepository::class.java)
        userRolesService = UserRolesService(landlordRepository, localAuthorityUserRepository)
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
        Assertions.assertEquals("ROLE_LANDLORD", roles[0])
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
        Assertions.assertEquals("ROLE_LA_ADMIN", roles[0])
        Assertions.assertEquals("ROLE_LA_USER", roles[1])
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
        Assertions.assertEquals("ROLE_LA_USER", roles[0])
    }
}
