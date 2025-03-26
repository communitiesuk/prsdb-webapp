package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthorityUser

@ExtendWith(MockitoExtension::class)
class LandlordDeregistrationServiceTests {
    @Mock
    private lateinit var mockLandlordRepository: LandlordRepository

    @Mock
    private lateinit var mockOneLoginUserRepository: OneLoginUserRepository

    @Mock
    private lateinit var mockLocalAuthorityUserRepository: LocalAuthorityUserRepository

    @InjectMocks
    private lateinit var landlordDeregistrationService: LandlordDeregistrationService

    @Test
    fun `deregisterLandlord deletes the user from the landlord table`() {
        val baseUserId = "one-login-user"

        landlordDeregistrationService.deregisterLandlord(baseUserId)

        verify(mockLandlordRepository).deleteByBaseUser_Id(baseUserId)
    }

    @Test
    fun `deregisterLandlord deletes the user from the one-login table if they are not a different type of user`() {
        // At time of writing, we only have landlord and local authority users, so this only checks the local authority user table
        val baseUserId = "one-login-user"
        whenever(mockLocalAuthorityUserRepository.findByBaseUser_Id(baseUserId)).thenReturn(null)

        landlordDeregistrationService.deregisterLandlord(baseUserId)

        verify(mockOneLoginUserRepository).deleteById(baseUserId)
    }

    @Test
    fun `deregisterLandlord does not attempt to delete the user from the one-login table if they are a local authority user`() {
        val baseUserId = "one-login-user"
        whenever(mockLocalAuthorityUserRepository.findByBaseUser_Id(baseUserId))
            .thenReturn(MockLocalAuthorityData.createLocalAuthorityUser())

        landlordDeregistrationService.deregisterLandlord(baseUserId)

        verify(mockOneLoginUserRepository, never()).deleteById(baseUserId)
    }
}
