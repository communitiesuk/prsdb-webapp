package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityUser
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository

class LocalAuthorityDataServiceTests {
    private lateinit var localAuthorityUsersRepository: LocalAuthorityUserRepository
    private lateinit var localAuthorityDataService: LocalAuthorityDataService

    @BeforeEach
    fun setup() {
        localAuthorityUsersRepository = Mockito.mock(LocalAuthorityUserRepository::class.java)
        localAuthorityDataService = LocalAuthorityDataService(localAuthorityUsersRepository)
    }

    @Test
    fun `getLocalAuthorityUsersForLocalAuthority returns a populated list of LocalAuthorityUserDataModel`() {
        // Arrange
        val baseUser1 = OneLoginUser()
        val localAuthorityTest = LocalAuthority()
        val localAuthorityUser1 = LocalAuthorityUser()
        ReflectionTestUtils.setField(localAuthorityTest, "id", 123)
        ReflectionTestUtils.setField(baseUser1, "name", "Test user 1")
        ReflectionTestUtils.setField(localAuthorityUser1, "isManager", true)
        ReflectionTestUtils.setField(localAuthorityUser1, "baseUser", baseUser1)
        ReflectionTestUtils.setField(localAuthorityUser1, "localAuthority", localAuthorityTest)
        Mockito.`when`(localAuthorityUsersRepository.findByLocalAuthority_Id(123)).thenReturn(listOf(localAuthorityUser1))

        // Act
        val laUserList = localAuthorityDataService.getLocalAuthorityUsersForLocalAuthority(123)

        // Assert
        Assertions.assertEquals(1, laUserList.size)
    }
}
