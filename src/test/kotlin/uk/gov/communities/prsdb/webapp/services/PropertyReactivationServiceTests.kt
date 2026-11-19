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
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.REACTIVATED_PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationElectricalSafetyDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationEpcDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationGasSafetyDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationLicensingDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationTenancyDetailsDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PropertyReactivationServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockActiveInactiveService: ActiveInactiveService

    @Mock
    private lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    private lateinit var mockSession: HttpSession

    @InjectMocks
    private lateinit var propertyReactivationService: PropertyReactivationService

    private val propertyOwnershipId = 1L
    private val uprn = 2L

    private val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId, isActive = false)

    @Test
    fun `reactivatePropertyIfAddressAvailable saves the details provided and then reactivates the property when its address is available`() {
        // Arrange
        val licensing = ReactivationLicensingDataModel(LicensingType.NO_LICENSING, null)
        val gasSafety = ReactivationGasSafetyDataModel(hasGasSupply = false, issueDate = null, uploadIds = emptyList())
        val electricalSafety =
            ReactivationElectricalSafetyDataModel(CertificateType.Eicr, LocalDate.of(2030, 1, 1), null, null, listOf(2L))
        val epc = ReactivationEpcDataModel("https://epc.example/cert", LocalDate.of(2031, 1, 1), "C", null, null, null)
        val tenancyDetails =
            ReactivationTenancyDetailsDataModel(
                1,
                2,
                null,
                null,
                FurnishedStatus.FURNISHED,
                RentFrequency.MONTHLY,
                null,
                BigDecimal("400"),
            )
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(
            propertyOwnershipId,
            uprn,
            PropertyReactivationDataModel(licensing, gasSafety, electricalSafety, epc, tenancyDetails),
        )

        // Assert
        inOrder(mockPropertyOwnershipService, mockPropertyComplianceService, mockActiveInactiveService) {
            verify(mockPropertyOwnershipService).saveReactivationLicensing(propertyOwnership, licensing)
            verify(mockPropertyOwnershipService).saveReactivationTenancyDetails(propertyOwnership, tenancyDetails)
            verify(mockPropertyComplianceService).saveReactivationComplianceData(propertyOwnershipId, gasSafety, electricalSafety, epc)
            verify(mockActiveInactiveService).reactivateProperty(propertyOwnershipId)
        }
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable doesn't save any details when the property had none to provide`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn, PropertyReactivationDataModel())

        // Assert
        verify(mockPropertyOwnershipService, never()).saveReactivationLicensing(any(), any())
        verify(mockPropertyOwnershipService, never()).saveReactivationTenancyDetails(any(), any())
        verifyNoInteractions(mockPropertyComplianceService)
        verify(mockActiveInactiveService).reactivateProperty(propertyOwnershipId)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable stores the property in the session as the last property reactivated`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)
        whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn, PropertyReactivationDataModel())

        // Assert
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable throws an EntityExistsException without saving or reactivating when the address is unavailable`() {
        // Arrange
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(true)

        // Act & Assert
        assertThrows<EntityExistsException> {
            propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, uprn, PropertyReactivationDataModel())
        }
        verifyNoInteractions(mockPropertyOwnershipService)
        verifyNoInteractions(mockPropertyComplianceService)
        verifyNoInteractions(mockActiveInactiveService)
        verifyNoInteractions(mockSession)
    }

    @Test
    fun `reactivatePropertyIfAddressAvailable reactivates the property without checking its address when it has no UPRN`() {
        // Arrange
        whenever(mockPropertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId))
            .thenReturn(propertyOwnership)

        // Act
        propertyReactivationService.reactivatePropertyIfAddressAvailable(propertyOwnershipId, null, PropertyReactivationDataModel())

        // Assert
        verify(mockActiveInactiveService).reactivateProperty(propertyOwnershipId)
        verify(mockSession).setAttribute(REACTIVATED_PROPERTY_OWNERSHIP_ID, propertyOwnershipId)
        verifyNoInteractions(mockAddressAvailabilityService)
    }
}
