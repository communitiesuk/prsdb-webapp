package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository

@ExtendWith(MockitoExtension::class)
class AddressAvailabilityServiceTests {
    @Mock
    lateinit var mockPropertyOwnershipRepository: PropertyOwnershipRepository

    @Test
    fun `isAddressOwnedByUser returns true when user owns the address`() {
        // Arrange
        val service = AddressAvailabilityService(mockPropertyOwnershipRepository)
        whenever(
            mockPropertyOwnershipRepository.existsByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndAddress_Uprn("user-1", 123L),
        ).thenReturn(true)

        // Act & Assert
        assertTrue(service.isAddressOwnedByUser(123L, "user-1"))
    }

    @Test
    fun `isAddressOwnedByUser returns false when user does not own the address`() {
        // Arrange
        val service = AddressAvailabilityService(mockPropertyOwnershipRepository)
        whenever(
            mockPropertyOwnershipRepository.existsByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndAddress_Uprn("user-1", 123L),
        ).thenReturn(false)

        // Act & Assert
        assertFalse(service.isAddressOwnedByUser(123L, "user-1"))
    }
}
