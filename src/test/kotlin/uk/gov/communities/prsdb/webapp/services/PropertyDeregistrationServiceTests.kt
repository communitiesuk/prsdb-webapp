package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_DEREGISTERED_THIS_SESSION
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
    fun `addDeregisteredPropertyAndOwnershipIdsToSession adds a property ownership Id to the ones stored in the session`() {
        // Arrange
        val propertyOwnershipId = 123.toLong()
        val existingPropertyOwnershipIds = mutableListOf(456, 789)
        whenever(mockHttpSession.getAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION)).thenReturn(existingPropertyOwnershipIds)

        // Act
        propertyDeregistrationService.addDeregisteredPropertyOwnershipIdToSession(propertyOwnershipId)

        // Assert
        verify(mockHttpSession).setAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION, existingPropertyOwnershipIds + propertyOwnershipId)
    }

    @Test
    fun `getDeregisteredPropertyOwnershipIdsFromSession returns a list of property ownership Ids in the session`() {
        // Arrange
        val deregisteredPropertyOwnershipIds = mutableListOf(456L, 789L)
        whenever(mockHttpSession.getAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION)).thenReturn(deregisteredPropertyOwnershipIds)

        // Act
        val results = propertyDeregistrationService.getDeregisteredPropertyOwnershipIdsFromSession()

        // Assert
        assertEquals(deregisteredPropertyOwnershipIds, results)
    }

    @Test
    fun `getDeregisteredPropertyOwnershipIdsFromSession returns empty list if no property ownerships were deregistered in the session`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(PROPERTIES_DEREGISTERED_THIS_SESSION)).thenReturn(null)

        // Act
        val results = propertyDeregistrationService.getDeregisteredPropertyOwnershipIdsFromSession()

        // Assert
        assertEquals(emptyList(), results)
    }
}
