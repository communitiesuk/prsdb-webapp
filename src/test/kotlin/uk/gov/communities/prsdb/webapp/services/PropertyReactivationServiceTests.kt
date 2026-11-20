package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityExistsException
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PropertyReactivationServiceTests {
    @Mock
    private lateinit var mockActiveInactiveService: ActiveInactiveService

    @Mock
    private lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    private lateinit var mockUserToLandlordService: UserToLandlordService

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
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(MockLandlordData.createIndividualLandlord())

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn)

        // Assert
        verify(mockActiveInactiveService).reactivateProperty(propertyOwnershipId)
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
        verify(mockSession).setAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION, false)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable throws an EntityExistsException without reactivating when the address is unavailable`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(true)

        // Act & Assert
        assertThrows<EntityExistsException> { propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn) }
        verifyNoInteractions(mockActiveInactiveService)
        verifyNoInteractions(mockUserToLandlordService)
        verifyNoInteractions(mockSession)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable reactivates the property without checking its address when it has no UPRN`() {
        // Arrange
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(MockLandlordData.createIndividualLandlord())

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, null)

        // Assert
        verify(mockActiveInactiveService).reactivateProperty(propertyOwnershipId)
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
        verify(mockSession).setAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION, false)
        verifyNoInteractions(mockAddressAvailabilityService)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable stores that the landlord was reactivated when the current landlord was inactive`() {
        // Arrange
        val inactiveLandlord = MockLandlordData.createIndividualLandlord()
        inactiveLandlord.deactivate(LocalDate.of(2026, 1, 1))
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(inactiveLandlord)

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, null)

        // Assert
        verify(mockSession).setAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION, true)
    }

    @Test
    fun `wasLandlordReactivatedByLastReactivation returns true when the session flag is true`() {
        // Arrange
        whenever(mockSession.getAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION)).thenReturn(true)

        // Act, Assert
        assertTrue(propertyReactivationService.wasLandlordReactivatedByLastReactivation())
    }

    @Test
    fun `wasLandlordReactivatedByLastReactivation returns false when the session flag is missing`() {
        // Arrange
        whenever(mockSession.getAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REACTIVATION)).thenReturn(null)

        // Act, Assert
        assertFalse(propertyReactivationService.wasLandlordReactivatedByLastReactivation())
    }
}
