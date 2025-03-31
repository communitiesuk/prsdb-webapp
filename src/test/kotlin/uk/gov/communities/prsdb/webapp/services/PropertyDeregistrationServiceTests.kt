package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DEREGISTRATION_ENTITY_IDS
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class PropertyDeregistrationServiceTests {
    @Mock
    private lateinit var mockPropertyService: PropertyService

    @Mock
    private lateinit var mockLicenceService: LicenseService

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @InjectMocks
    private lateinit var propertyDeregistrationService: PropertyDeregistrationService

    @Test
    fun `deregisterProperty deletes the property, license and property ownership`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val propertyOwnershipId = propertyOwnership.id
        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)).thenReturn(propertyOwnership)
        // Act
        propertyDeregistrationService.deregisterProperty(propertyOwnershipId)

        verify(mockPropertyOwnershipService).deletePropertyOwnership(propertyOwnership)
        verify(mockPropertyService).deleteProperty(propertyOwnership.property)
        verify(mockLicenceService).deleteLicense(propertyOwnership.license)
    }

    @Test
    fun `addDeregisteredPropertyAndOwnershipIdsToSession appends a new pair of entity ids if some are already stored in the session`() {
        // Arrange
        val propertyOwnershipId = 123.toLong()
        val propertyId = 234.toLong()
        val existingPropertyEntityIdPairs =
            mutableListOf(
                Pair(456.toLong(), 567.toLong()),
                Pair(789.toLong(), 890.toLong()),
            )
        whenever(mockHttpSession.getAttribute(PROPERTY_DEREGISTRATION_ENTITY_IDS)).thenReturn(existingPropertyEntityIdPairs)

        // Act
        propertyDeregistrationService.addDeregisteredPropertyAndOwnershipIdsToSession(propertyOwnershipId, propertyId)

        // Assert
        verify(mockHttpSession).setAttribute(
            PROPERTY_DEREGISTRATION_ENTITY_IDS,
            mutableListOf(
                Pair(456.toLong(), 567.toLong()),
                Pair(789.toLong(), 890.toLong()),
                Pair(123.toLong(), 234.toLong()),
            ),
        )
    }

    @Test
    fun `addDeregisteredPropertyAndOwnershipIdsToSession stores a list of entity pairs in the session`() {
        // Arrange
        val propertyOwnershipId = 123.toLong()
        val propertyId = 234.toLong()
        whenever(mockHttpSession.getAttribute(PROPERTY_DEREGISTRATION_ENTITY_IDS))
            .thenReturn(mutableListOf<Pair<Long, Long>>())

        // Act
        propertyDeregistrationService.addDeregisteredPropertyAndOwnershipIdsToSession(propertyOwnershipId, propertyId)

        // Assert
        verify(mockHttpSession).setAttribute(PROPERTY_DEREGISTRATION_ENTITY_IDS, mutableListOf(Pair(propertyOwnershipId, propertyId)))
    }
}
