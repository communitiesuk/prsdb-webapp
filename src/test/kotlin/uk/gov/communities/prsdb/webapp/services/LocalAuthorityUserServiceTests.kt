package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthorityUser
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.createOneLoginUser

@ExtendWith(MockitoExtension::class)
class LocalAuthorityUserServiceTests {
    @Mock
    private lateinit var mockLocalAuthorityUserRepository: LocalAuthorityUserRepository

    @InjectMocks
    private lateinit var localAuthorityUserService: LocalAuthorityUserService

    @Test
    fun `getIsLocalAuthorityUser returns true when the user is a local authority user`() {
        val localAuthorityUser = createLocalAuthorityUser(createOneLoginUser(), createLocalAuthority())
        val baseUserId = localAuthorityUser.baseUser.id

        whenever(mockLocalAuthorityUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(localAuthorityUser)

        assertTrue(localAuthorityUserService.getIsLocalAuthorityUser(baseUserId))
    }

    @Test
    fun `getIsLocalAuthorityUser returns false when the user is not a local authority user`() {
        val baseUserId = "not-an-la-user"

        whenever(mockLocalAuthorityUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)

        assertFalse(localAuthorityUserService.getIsLocalAuthorityUser(baseUserId))
    }
}
