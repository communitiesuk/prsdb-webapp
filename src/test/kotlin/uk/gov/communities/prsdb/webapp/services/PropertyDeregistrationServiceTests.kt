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
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class PropertyDeregistrationServiceTests {
    @Mock
    private lateinit var mockLicenceService: LicenseService

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @InjectMocks
    private lateinit var propertyDeregistrationService: PropertyDeregistrationService

    @Mock
    private lateinit var mockFormContextService: FormContextService

    @Test
    fun `deregisterProperty deletes the license, compliance and property ownership`() {
        val licence = License()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(license = licence)
        val propertyOwnershipId = propertyOwnership.id
        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)).thenReturn(propertyOwnership)
        // Act
        propertyDeregistrationService.deregisterProperty(propertyOwnershipId)

        verify(mockPropertyComplianceService).deletePropertyComplianceByOwnershipId(propertyOwnership.id)
        verify(mockPropertyOwnershipService).deletePropertyOwnership(propertyOwnership)
        verify(mockLicenceService).deleteLicense(licence)
    }

    @Test
    fun `deregisterProperties deletes the license, compliance and property ownership for all properties`() {
        val license = License()
        val propertyOwnerships =
            listOf(
                MockLandlordData.createPropertyOwnership(license = license),
                MockLandlordData.createPropertyOwnership(),
            )

        val propertyOwnershipIds = propertyOwnerships.map { it.id }

        propertyDeregistrationService.deregisterProperties(propertyOwnerships)

        verify(mockPropertyComplianceService).deletePropertyCompliancesByOwnershipIds(propertyOwnershipIds)
        verify(mockPropertyOwnershipService).deletePropertyOwnerships(propertyOwnerships)
        verify(mockLicenceService).deleteLicenses(listOf(license))
    }

    @Test
    fun `deregisterProperty deletes the incompleteComplianceForm for the property ownership`() {
        val incompleteCompliance = MockLandlordData.createPropertyComplianceFormContext()
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                incompleteComplianceForm = incompleteCompliance,
            )
        val propertyOwnershipId = propertyOwnership.id
        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)).thenReturn(propertyOwnership)
        // Act
        propertyDeregistrationService.deregisterProperty(propertyOwnershipId)

        verify(mockFormContextService).deleteFormContext(incompleteCompliance)
        verify(mockPropertyOwnershipService).deletePropertyOwnership(propertyOwnership)
    }

    @Test
    fun `deregisterProperties deletes the incompleteComplianceForms for all properties`() {
        val incompleteCompliances =
            listOf(
                MockLandlordData.createPropertyComplianceFormContext(),
                MockLandlordData.createPropertyComplianceFormContext(),
            )
        val propertyOwnerships =
            listOf(
                MockLandlordData.createPropertyOwnership(incompleteComplianceForm = incompleteCompliances[0]),
                MockLandlordData.createPropertyOwnership(incompleteComplianceForm = incompleteCompliances[1]),
            )

        propertyDeregistrationService.deregisterProperties(propertyOwnerships)

        verify(mockFormContextService).deleteFormContexts(incompleteCompliances)
        verify(mockPropertyOwnershipService).deletePropertyOwnerships(propertyOwnerships)
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
