package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID

@ExtendWith(MockitoExtension::class)
class PropertyReactivationServiceTests {
    @Mock
    private lateinit var mockActivationService: ActivationService

    @Mock
    private lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    private lateinit var mockSession: HttpSession

    @InjectMocks
    private lateinit var propertyReactivationService: PropertyReactivationService

    private val propertyOwnershipId = 1L
    private val uprn = 2L

    @Test
    fun `reactivatePropertyIfAddressAvailable reactivates the property and stores it in the session when its address is available`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn)

        // Assert
        verify(mockActivationService).reactivateProperty(propertyOwnershipId)
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable throws an EntityExistsException without reactivating when the address is unavailable`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(true)

        // Act & Assert
        assertThrows<EntityExistsException> { propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn) }
        verifyNoInteractions(mockActivationService)
        verifyNoInteractions(mockSession)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable reactivates the property without checking its address when it has no UPRN`() {
        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, null)

        // Assert
        verify(mockActivationService).reactivateProperty(propertyOwnershipId)
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
        verifyNoInteractions(mockAddressAvailabilityService)
    }
}
