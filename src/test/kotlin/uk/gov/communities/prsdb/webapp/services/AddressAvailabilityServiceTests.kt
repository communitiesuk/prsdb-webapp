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
    fun `isAddressOwned returns true when address is registered`() {
        val service = AddressAvailabilityService(mockPropertyOwnershipRepository)
        whenever(mockPropertyOwnershipRepository.existsByIsActiveTrueAndAddress_Uprn(123L)).thenReturn(true)

        assertTrue(service.isAddressOwned(123L))
    }

    @Test
    fun `isAddressOwned returns false when address is not registered`() {
        val service = AddressAvailabilityService(mockPropertyOwnershipRepository)
        whenever(mockPropertyOwnershipRepository.existsByIsActiveTrueAndAddress_Uprn(123L)).thenReturn(false)

        assertFalse(service.isAddressOwned(123L))
    }
}
