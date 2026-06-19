package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_DEREGISTERED_THIS_SESSION_WITH_ADDRESSES
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class PropertyDeregistrationServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @InjectMocks
    private lateinit var propertyDeregistrationService: PropertyDeregistrationService

    @Test
    fun `deregisterProperty deletes the property ownership`() {
        // Arrange
        val propertyOwnershipId = 1L

        // Act
        propertyDeregistrationService.deregisterProperty(propertyOwnershipId)

        // Assert
        verify(mockPropertyOwnershipService).deletePropertyOwnership(propertyOwnershipId)
    }

    @Test
    fun `addDeregisteredPropertyOwnershipIdToSession adds the id and address to the ones stored in the session`() {
        // Arrange
        val propertyOwnershipId = 123.toLong()
        val address = "Flat 1, 1 Elm Street, London, NE1 2GB"
        val existingDeregisteredProperties = mutableMapOf(456L to "456 Road", 789L to "789 Road")
        whenever(
            mockHttpSession.getAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION_WITH_ADDRESSES),
        ).thenReturn(existingDeregisteredProperties)

        // Act
        propertyDeregistrationService.addDeregisteredPropertyOwnershipIdToSession(propertyOwnershipId, address)

        // Assert
        verify(mockHttpSession).setAttribute(
            PROPERTIES_DEREGISTERED_THIS_SESSION_WITH_ADDRESSES,
            existingDeregisteredProperties + (propertyOwnershipId to address),
        )
    }

    @Test
    fun `addDeregisteredPropertyOwnershipIdToSession adds the id with a null address when no address is given`() {
        // Arrange
        val propertyOwnershipId = 123.toLong()
        val existingDeregisteredProperties = mutableMapOf<Long, String?>(456L to "456 Road", 789L to "789 Road")
        whenever(
            mockHttpSession.getAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION_WITH_ADDRESSES),
        ).thenReturn(existingDeregisteredProperties)

        // Act
        propertyDeregistrationService.addDeregisteredPropertyOwnershipIdToSession(propertyOwnershipId)

        // Assert
        verify(mockHttpSession).setAttribute(
            PROPERTIES_DEREGISTERED_THIS_SESSION_WITH_ADDRESSES,
            existingDeregisteredProperties + (propertyOwnershipId to null),
        )
    }

    @Test
    fun `getDeregisteredPropertyOwnershipIdsFromSession returns a list of property ownership Ids in the session`() {
        // Arrange
        val deregisteredProperties = mutableMapOf(456L to "456 Road", 789L to "789 Road")
        whenever(mockHttpSession.getAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION_WITH_ADDRESSES)).thenReturn(deregisteredProperties)

        // Act
        val results = propertyDeregistrationService.getDeregisteredPropertyOwnershipIdsFromSession()

        // Assert
        assertEquals(mutableListOf(456L, 789L), results)
    }

    @Test
    fun `getDeregisteredPropertyOwnershipIdsFromSession returns empty list if no property ownerships were deregistered in the session`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION_WITH_ADDRESSES)).thenReturn(null)

        // Act
        val results = propertyDeregistrationService.getDeregisteredPropertyOwnershipIdsFromSession()

        // Assert
        assertEquals(emptyList(), results)
    }

    @Test
    fun `getDeregisteredPropertyAddress returns the stored address for the property ownership Id`() {
        // Arrange
        val deregisteredProperties = mutableMapOf(456L to "456 Road", 789L to "789 Road")
        whenever(mockHttpSession.getAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION_WITH_ADDRESSES)).thenReturn(deregisteredProperties)

        // Act
        val result = propertyDeregistrationService.getDeregisteredPropertyAddress(456L)

        // Assert
        assertEquals("456 Road", result)
    }

    @Test
    fun `getDeregisteredPropertyAddress returns null if the property ownership Id is not in the session`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION_WITH_ADDRESSES)).thenReturn(null)

        // Act
        val result = propertyDeregistrationService.getDeregisteredPropertyAddress(456L)

        // Assert
        assertEquals(null, result)
    }
}
