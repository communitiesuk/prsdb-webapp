package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class LandlordDeregistrationServiceTests {
    @Mock
    private lateinit var mockLandlordRepository: LandlordRepository

    @Mock
    private lateinit var mockOneLoginUserRepository: OneLoginUserRepository

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyDeregistrationService: PropertyDeregistrationService

    @InjectMocks
    private lateinit var landlordDeregistrationService: LandlordDeregistrationService

    @Test
    fun `deregisterLandlordAndTheirProperties deletes the user from the landlord table`() {
        val baseUserId = "one-login-user"

        landlordDeregistrationService.deregisterLandlordAndTheirProperties(baseUserId)

        verify(mockLandlordRepository).deleteByBaseUser_Id(baseUserId)
    }

    @Test
    fun `deregisterLandlordAndTheirProperties deletes the user from the one-login table if they are not a different type of user`() {
        // At time of writing, we only have landlord and local authority users, so this only checks the local authority user table
        val baseUserId = "one-login-user"

        landlordDeregistrationService.deregisterLandlordAndTheirProperties(baseUserId)

        verify(mockOneLoginUserRepository).deleteIfNotLocalAuthorityUser(baseUserId)
    }

    @Test
    fun `deregisterLandlordAndTheirProperties deletes landlord properties`() {
        // Arrange
        val landlord = MockLandlordData.createLandlord(baseUser = MockLandlordData.createOneLoginUser(id = "one-login-user"))
        val landlordProperties =
            listOf(
                MockLandlordData.createPropertyOwnership(primaryLandlord = landlord),
                MockLandlordData.createPropertyOwnership(primaryLandlord = landlord),
            )
        whenever(mockPropertyOwnershipService.retrieveAllPropertiesForLandlord("one-login-user")).thenReturn(landlordProperties)

        // Act
        landlordDeregistrationService.deregisterLandlordAndTheirProperties("one-login-user")

        // Assert
        verify(mockPropertyDeregistrationService).deregisterProperties(landlordProperties)
    }

    @Test
    fun `deregisterLandlordAndTheirProperties does not attempt to delete landlord properties if there are none to delete`() {
        val baseUserId = "one-login-user"

        landlordDeregistrationService.deregisterLandlordAndTheirProperties(baseUserId)

        verify(mockPropertyDeregistrationService, never()).deregisterProperties(any())
    }
}
