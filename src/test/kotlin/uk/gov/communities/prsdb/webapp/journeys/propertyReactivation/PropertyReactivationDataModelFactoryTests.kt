package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.LicensingAndTenancyDetailsDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.TenancyDetailsTask
import uk.gov.communities.prsdb.webapp.models.dataModels.LicensingDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationElectricalSafetyDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationEpcDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReactivationGasSafetyDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.TenancyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcInDateAtStartOfTenancyCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class PropertyReactivationDataModelFactoryTests {
    @Mock
    private lateinit var mockLicensingAndTenancyDetailsDataModelFactory: LicensingAndTenancyDetailsDataModelFactory

    @Mock
    private lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    @Mock
    private lateinit var mockState: PropertyReactivationJourneyState

    private lateinit var factory: PropertyReactivationDataModelFactory

    @BeforeEach
    fun setUp() {
        factory = PropertyReactivationDataModelFactory(mockLicensingAndTenancyDetailsDataModelFactory, mockEpcCertificateUrlProvider)
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
    fun `fromJourneyState maps the gas safety answers when gas safety was required`() {
        // Arrange
        whenever(mockState.requiredTaskTypes).thenReturn(listOf(ReactivationTaskType.GAS_SAFETY))
        val gasSafetyDetailsTask = setUpGasSafety(HasGasSupplyMode.HAS_SUPPLY)
        whenever(gasSafetyDetailsTask.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(LocalDate(2025, 1, 2))
        whenever(gasSafetyDetailsTask.gasUploadIds).thenReturn(listOf(1L, 2L))

        // Act
        val result = factory.fromJourneyState(mockState)

        // Assert
        assertEquals(
            PropertyReactivationDataModel(
                gasSafety = ReactivationGasSafetyDataModel(true, LocalDate(2025, 1, 2).toJavaLocalDate(), listOf(1L, 2L)),
            ),
            result,
        )
    }

    @Test
    fun `fromJourneyState throws when the gas supply question is to be answered later`() {
        // Arrange
        whenever(mockState.requiredTaskTypes).thenReturn(listOf(ReactivationTaskType.GAS_SAFETY))
        setUpGasSafety(HasGasSupplyMode.PROVIDE_LATER)

        // Act & Assert
        assertThrows<IllegalStateException> { factory.fromJourneyState(mockState) }
    }

    @Test
    fun `fromJourneyState maps the electrical safety answers when electrical safety was required`() {
        // Arrange
        whenever(mockState.requiredTaskTypes).thenReturn(listOf(ReactivationTaskType.ELECTRICAL_SAFETY))
        val mockElectricalSafetyTask = mock<ElectricalSafetyTask>()
        val mockDetailsTask = mock<ElectricalSafetyDetailsTask>()
        val mockHasElectricalCertStep = mock<HasElectricalCertStep>()
        whenever(mockState.electricalSafetyTask).thenReturn(mockElectricalSafetyTask)
        whenever(mockElectricalSafetyTask.electricalSafetyDetailsTask).thenReturn(mockDetailsTask)
        whenever(mockDetailsTask.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)
        whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EICR)
        whenever(mockDetailsTask.mapElectricalCertificateTypeToGlobalCertificateType()).thenReturn(CertificateType.Eicr)
        whenever(mockDetailsTask.getEicrExpiryDateIfReachable()).thenReturn(LocalDate(2030, 5, 6))
        whenever(mockDetailsTask.electricalUploadIds).thenReturn(listOf(3L))

        // Act
        val result = factory.fromJourneyState(mockState)

        // Assert
        assertEquals(
            PropertyReactivationDataModel(
                electricalSafety =
                    ReactivationElectricalSafetyDataModel(
                        certType = CertificateType.Eicr,
                        eicrExpiryDate = LocalDate(2030, 5, 6).toJavaLocalDate(),
                        eicIssueDate = null,
                        eicExpiryDate = null,
                        uploadIds = listOf(3L),
                    ),
            ),
            result,
        )
    }

    @Test
    fun `fromJourneyState maps the EPC answers when the EPC was required`() {
        // Arrange
        whenever(mockState.requiredTaskTypes).thenReturn(listOf(ReactivationTaskType.EPC))
        val acceptedEpc = MockEpcData.createEpcDataModel()
        val mockEpcTask = mock<EpcTask>()
        val mockDetailsTask = mock<EpcDetailsTask>()
        val mockHasEpcStep = mock<HasEpcStep>()
        val mockTenancyCheckStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
        val mockEpcExemptionStep = mock<EpcExemptionStep>()
        val mockMeesExemptionStep = mock<MeesExemptionStep>()
        whenever(mockState.epcTask).thenReturn(mockEpcTask)
        whenever(mockEpcTask.epcDetailsTask).thenReturn(mockDetailsTask)
        whenever(mockDetailsTask.hasEpcStep).thenReturn(mockHasEpcStep)
        whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.HAS_EPC)
        whenever(mockDetailsTask.acceptedEpcIfStillAccepted).thenReturn(acceptedEpc)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(acceptedEpc.certificateNumber)).thenReturn("https://epc.example/cert")
        whenever(mockDetailsTask.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyCheckStep)
        whenever(mockTenancyCheckStep.formModelIfReachableOrNull)
            .thenReturn(EpcInDateAtStartOfTenancyCheckFormModel().apply { tenancyStartedBeforeExpiry = true })
        whenever(mockDetailsTask.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockDetailsTask.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull)
            .thenReturn(MeesExemptionReasonFormModel().apply { exemptionReason = MeesExemptionReason.HIGH_COST })

        // Act
        val result = factory.fromJourneyState(mockState)

        // Assert
        assertEquals(
            PropertyReactivationDataModel(
                epc =
                    ReactivationEpcDataModel(
                        certificateUrl = "https://epc.example/cert",
                        expiryDate = acceptedEpc.expiryDateAsJavaLocalDate,
                        energyRating = acceptedEpc.energyRating,
                        tenancyStartedBeforeExpiry = true,
                        exemptionReason = null,
                        meesExemptionReason = MeesExemptionReason.HIGH_COST,
                    ),
            ),
            result,
        )
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

    private fun setUpGasSafety(hasGasSupplyOutcome: HasGasSupplyMode?): GasSafetyDetailsTask {
        val mockGasSafetyTask = mock<GasSafetyTask>()
        val mockDetailsTask = mock<GasSafetyDetailsTask>()
        val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
        whenever(mockState.gasSafetyTask).thenReturn(mockGasSafetyTask)
        whenever(mockGasSafetyTask.gasSafetyDetailsTask).thenReturn(mockDetailsTask)
        whenever(mockDetailsTask.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
        whenever(mockHasGasSupplyStep.outcome).thenReturn(hasGasSupplyOutcome)
        return mockDetailsTask
    }
}
