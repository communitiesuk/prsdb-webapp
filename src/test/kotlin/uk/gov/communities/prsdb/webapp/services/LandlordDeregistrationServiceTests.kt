package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository

@ExtendWith(MockitoExtension::class)
class LandlordDeregistrationServiceTests {
    @Mock
    private lateinit var mockLandlordRepository: LandlordRepository

    @Mock
    private lateinit var mockOneLoginUserRepository: OneLoginUserRepository

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

        landlordDeregistrationService.deregisterLandlord(baseUserId)

        verify(mockOneLoginUserRepository).deleteIfNotLocalAuthorityUser(baseUserId)
    }
}
