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
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData

@ExtendWith(MockitoExtension::class)
class PropertyDeregistrationServiceTests {
    @Mock
    private lateinit var mockPropertyService: PropertyService

    @Mock
    private lateinit var mockLicenceService: LicenseService

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @InjectMocks
    @Mock
    private lateinit var propertyDeregistrationService: PropertyDeregistrationService

    @Mock
    private lateinit var mockFormContextService: FormContextService

    @Test
    fun `deregisterProperty deletes the property, license, compliance and property ownership`() {
        val licence = License()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(license = licence)
        val propertyOwnershipId = propertyOwnership.id
        whenever(mockPropertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)).thenReturn(propertyOwnership)
        // Act
        propertyDeregistrationService.deregisterProperty(propertyOwnershipId)

        verify(mockPropertyComplianceService).deletePropertyComplianceIfExists(propertyOwnership.id)
        verify(mockPropertyOwnershipService).deletePropertyOwnership(propertyOwnership)
        verify(mockPropertyService).deleteProperty(propertyOwnership.property)
        verify(mockLicenceService).deleteLicense(licence)
    }

    @Test
    fun `deregisterProperties deletes the property, license, compliance and property ownership for all properties`() {
        val license = License()
        val properties =
            listOf(
                MockLandlordData.createProperty(),
                MockLandlordData.createProperty(),
            )
        val propertyOwnerships =
            listOf(
                MockLandlordData.createPropertyOwnership(license = license, property = properties[0]),
                MockLandlordData.createPropertyOwnership(property = properties[1]),
            )
        val propertyCompliances =
            listOf(
                MockPropertyComplianceData.createPropertyCompliance(propertyOwnership = propertyOwnerships[0]),
                MockPropertyComplianceData.createPropertyCompliance(propertyOwnership = propertyOwnerships[1]),
            )

        whenever(
            mockPropertyComplianceService.getPropertyCompliancesForPropertyOwnerships(propertyOwnerships),
        ).thenReturn(propertyCompliances)

        propertyDeregistrationService.deregisterProperties(propertyOwnerships)

        verify(mockPropertyComplianceService).getPropertyCompliancesForPropertyOwnerships(propertyOwnerships)
        verify(mockPropertyComplianceService).deletePropertyCompliances(propertyCompliances)
        verify(mockPropertyOwnershipService).deletePropertyOwnerships(propertyOwnerships)
        verify(mockPropertyService).deleteProperties(properties)
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
