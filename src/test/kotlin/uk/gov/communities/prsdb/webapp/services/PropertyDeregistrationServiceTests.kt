package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DEREGISTERED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
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
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        // Act
        propertyDeregistrationService.deregisterProperty(propertyOwnershipId)

        // Assert
        verify(mockPropertyOwnershipService).deletePropertyOwnership(propertyOwnershipId)
    }

    @Test
    fun `deregisterProperty returns the email details for the deregistered property`() {
        // Arrange
        val propertyOwnershipId = 1L
        val landlordEmail = "landlord@example.com"
        val landlord = MockLandlordData.createLandlord(email = landlordEmail)
        val singleLineAddress = "123 Test Street, AB1 2CD"
        val address = MockLandlordData.createAddress(singleLineAddress = singleLineAddress)
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = propertyOwnershipId,
                primaryLandlord = landlord,
                address = address,
            )
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

        // Act
        val result = propertyDeregistrationService.deregisterProperty(propertyOwnershipId)

        // Assert
        assertEquals(listOf(landlordEmail), result.landlordEmailAddresses)
        assertEquals(singleLineAddress, result.singleLineAddress)
    }

    @Test
    fun `setDeregisteredPropertyInSession stores the id and address in the session`() {
        // Arrange
        val propertyOwnershipId = 123.toLong()
        val address = "Flat 1, 1 Elm Street, London, NE1 2GB"

        // Act
        propertyDeregistrationService.setDeregisteredPropertyInSession(propertyOwnershipId, address)

        // Assert
        verify(mockHttpSession).setAttribute(
            PROPERTY_DEREGISTERED_THIS_SESSION,
            propertyOwnershipId to address,
        )
    }

    @Test
    fun `getDeregisteredPropertyOwnershipIdFromSession returns the id stored in the session`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(PROPERTY_DEREGISTERED_THIS_SESSION)).thenReturn(456L to "456 Road")

        // Act
        val result = propertyDeregistrationService.getDeregisteredPropertyOwnershipIdFromSession()

        // Assert
        assertEquals(456L, result)
    }

    @Test
    fun `getDeregisteredPropertyOwnershipIdFromSession returns null if no property was deregistered in the session`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(PROPERTY_DEREGISTERED_THIS_SESSION)).thenReturn(null)

        // Act
        val result = propertyDeregistrationService.getDeregisteredPropertyOwnershipIdFromSession()

        // Assert
        assertEquals(null, result)
    }

    @Test
    fun `getDeregisteredPropertyAddress returns the stored address`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(PROPERTY_DEREGISTERED_THIS_SESSION)).thenReturn(456L to "456 Road")

        // Act
        val result = propertyDeregistrationService.getDeregisteredPropertyAddress()

        // Assert
        assertEquals("456 Road", result)
    }

    @Test
    fun `getDeregisteredPropertyAddress returns null if no property was deregistered in the session`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(PROPERTY_DEREGISTERED_THIS_SESSION)).thenReturn(null)

        // Act
        val result = propertyDeregistrationService.getDeregisteredPropertyAddress()

        // Assert
        assertEquals(null, result)
    }
}
