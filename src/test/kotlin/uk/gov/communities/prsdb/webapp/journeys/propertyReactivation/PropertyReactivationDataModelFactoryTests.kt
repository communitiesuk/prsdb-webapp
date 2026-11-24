package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.ComplianceDetailsDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.LicensingAndTenancyDetailsDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.TenancyDetailsTask
import uk.gov.communities.prsdb.webapp.models.dataModels.ElectricalSafetyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GasSafetyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LicensingDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.TenancyDetailsDataModel
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class PropertyReactivationDataModelFactoryTests {
    @Mock
    private lateinit var mockLicensingAndTenancyDetailsDataModelFactory: LicensingAndTenancyDetailsDataModelFactory

    @Mock
    private lateinit var mockComplianceDetailsDataModelFactory: ComplianceDetailsDataModelFactory

    @Mock
    private lateinit var mockState: PropertyReactivationJourneyState

    private lateinit var factory: PropertyReactivationDataModelFactory

    @BeforeEach
    fun setUp() {
        factory =
            PropertyReactivationDataModelFactory(mockLicensingAndTenancyDetailsDataModelFactory, mockComplianceDetailsDataModelFactory)
    }

    @Test
    fun `fromJourneyState leaves every section empty when the property had no details to provide`() {
        // Arrange
        whenever(mockState.requiredTaskTypes).thenReturn(emptyList())

        // Act
        val result = factory.fromJourneyState(mockState)

        // Assert
        assertEquals(PropertyReactivationDataModel(), result)
    }

    @Test
    fun `fromJourneyState populates the licensing section and leaves the others empty when only licensing was required`() {
        // Arrange
        whenever(mockState.requiredTaskTypes).thenReturn(listOf(ReactivationTaskType.LICENSING))
        val licensingTask = mock<LicensingTask>()
        whenever(mockState.licensingTask).thenReturn(licensingTask)
        val licensing = LicensingDetailsDataModel(LicensingType.SELECTIVE_LICENCE, "SL-123")
        whenever(mockLicensingAndTenancyDetailsDataModelFactory.fromLicensingState(licensingTask)).thenReturn(licensing)

        // Act
        val result = factory.fromJourneyState(mockState)

        // Assert
        assertEquals(PropertyReactivationDataModel(licensing = licensing), result)
    }

    @Test
    fun `fromJourneyState populates the compliance sections from their details tasks when they were required`() {
        // Arrange
        whenever(mockState.requiredTaskTypes)
            .thenReturn(listOf(ReactivationTaskType.GAS_SAFETY, ReactivationTaskType.ELECTRICAL_SAFETY, ReactivationTaskType.EPC))
        val gasSafetyTask = mock<GasSafetyTask>()
        val gasSafetyDetailsTask = mock<GasSafetyDetailsTask>()
        whenever(mockState.gasSafetyTask).thenReturn(gasSafetyTask)
        whenever(gasSafetyTask.gasSafetyDetailsTask).thenReturn(gasSafetyDetailsTask)
        val electricalSafetyTask = mock<ElectricalSafetyTask>()
        val electricalSafetyDetailsTask = mock<ElectricalSafetyDetailsTask>()
        whenever(mockState.electricalSafetyTask).thenReturn(electricalSafetyTask)
        whenever(electricalSafetyTask.electricalSafetyDetailsTask).thenReturn(electricalSafetyDetailsTask)
        val epcTask = mock<EpcTask>()
        val epcDetailsTask = mock<EpcDetailsTask>()
        whenever(mockState.epcTask).thenReturn(epcTask)
        whenever(epcTask.epcDetailsTask).thenReturn(epcDetailsTask)

        val gasSafety = GasSafetyDetailsDataModel(hasGasSupply = false, issueDate = null, uploadIds = emptyList())
        val electricalSafety = ElectricalSafetyDetailsDataModel(CertificateType.Eicr, null, null, null, listOf(3L))
        val epc = EpcDetailsDataModel(null, null, null, null, EpcExemptionReason.DUE_FOR_DEMOLITION, null)
        whenever(mockComplianceDetailsDataModelFactory.fromGasSafetyDetailState(gasSafetyDetailsTask)).thenReturn(gasSafety)
        whenever(mockComplianceDetailsDataModelFactory.fromElectricalSafetyDetailState(electricalSafetyDetailsTask))
            .thenReturn(electricalSafety)
        whenever(mockComplianceDetailsDataModelFactory.fromEpcDetailState(epcDetailsTask)).thenReturn(epc)

        // Act
        val result = factory.fromJourneyState(mockState)

        // Assert
        assertEquals(PropertyReactivationDataModel(gasSafety = gasSafety, electricalSafety = electricalSafety, epc = epc), result)
    }

    @Test
    fun `fromJourneyState populates the tenancy details section and leaves the others empty when only tenancy details were required`() {
        // Arrange
        whenever(mockState.requiredTaskTypes).thenReturn(listOf(ReactivationTaskType.TENANCY_DETAILS))
        val tenancyDetailsTask = mock<TenancyDetailsTask>()
        whenever(mockState.tenancyDetailsTask).thenReturn(tenancyDetailsTask)
        val tenancyDetails =
            TenancyDetailsDataModel(2, 4, null, null, FurnishedStatus.FURNISHED, RentFrequency.MONTHLY, null, BigDecimal("400.50"))
        whenever(mockLicensingAndTenancyDetailsDataModelFactory.fromTenancyDetailsState(tenancyDetailsTask)).thenReturn(tenancyDetails)

        // Act
        val result = factory.fromJourneyState(mockState)

        // Assert
        assertEquals(PropertyReactivationDataModel(tenancyDetails = tenancyDetails), result)
    }
}
