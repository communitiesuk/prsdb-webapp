package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.models.dataModels.ElectricalSafetyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GasSafetyDetailsDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcInDateAtStartOfTenancyCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

@ExtendWith(MockitoExtension::class)
class ComplianceDetailsDataModelFactoryTests {
    @Mock
    private lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    private lateinit var factory: ComplianceDetailsDataModelFactory

    @BeforeEach
    fun setUp() {
        factory = ComplianceDetailsDataModelFactory(mockEpcCertificateUrlProvider)
    }

    @Nested
    inner class FromGasSafetyDetailState {
        @Mock
        private lateinit var mockDetailsTask: GasSafetyDetailsTask

        @Mock
        private lateinit var mockHasGasSupplyStep: HasGasSupplyStep

        @BeforeEach
        fun setUp() {
            whenever(mockDetailsTask.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
        }

        @Test
        fun `maps the gas supply, issue date and upload ids`() {
            // Arrange
            whenever(mockHasGasSupplyStep.outcome).thenReturn(HasGasSupplyMode.HAS_SUPPLY)
            whenever(mockDetailsTask.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(LocalDate(2025, 1, 2))
            whenever(mockDetailsTask.gasUploadIds).thenReturn(listOf(1L, 2L))

            // Act
            val result = factory.fromGasSafetyDetailState(mockDetailsTask)

            // Assert
            assertEquals(GasSafetyDetailsDataModel(true, LocalDate(2025, 1, 2).toJavaLocalDate(), listOf(1L, 2L)), result)
        }

        @Test
        fun `maps a property with no gas supply`() {
            // Arrange
            whenever(mockHasGasSupplyStep.outcome).thenReturn(HasGasSupplyMode.NO_SUPPLY)
            whenever(mockDetailsTask.gasUploadIds).thenReturn(emptyList())

            // Act
            val result = factory.fromGasSafetyDetailState(mockDetailsTask)

            // Assert
            assertEquals(GasSafetyDetailsDataModel(false, null, emptyList()), result)
        }

        @Test
        fun `throws when the gas supply question has not been answered`() {
            // Arrange
            whenever(mockHasGasSupplyStep.outcome).thenReturn(null)

            // Act & Assert
            assertThrows<NotNullFormModelValueIsNullException> { factory.fromGasSafetyDetailState(mockDetailsTask) }
        }

        @Test
        fun `throws when gas safety is to be provided later`() {
            // Arrange
            whenever(mockHasGasSupplyStep.outcome).thenReturn(HasGasSupplyMode.PROVIDE_LATER)

            // Act
            val exception = assertThrows<IllegalStateException> { factory.fromGasSafetyDetailState(mockDetailsTask) }

            // Assert
            assertEquals("Gas safety can only be provided later when registering a property", exception.message)
        }
    }

    @Nested
    inner class FromElectricalSafetyDetailState {
        @Mock
        private lateinit var mockDetailsTask: ElectricalSafetyDetailsTask

        @Mock
        private lateinit var mockHasElectricalCertStep: HasElectricalCertStep

        @BeforeEach
        fun setUp() {
            whenever(mockDetailsTask.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)
        }

        @Test
        fun `maps an EICR's expiry date and upload ids`() {
            // Arrange
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EICR)
            whenever(mockDetailsTask.mapElectricalCertificateTypeToGlobalCertificateType()).thenReturn(CertificateType.Eicr)
            whenever(mockDetailsTask.getEicrExpiryDateIfReachable()).thenReturn(LocalDate(2030, 5, 6))
            whenever(mockDetailsTask.electricalUploadIds).thenReturn(listOf(3L))

            // Act
            val result = factory.fromElectricalSafetyDetailState(mockDetailsTask)

            // Assert
            assertEquals(
                ElectricalSafetyDetailsDataModel(
                    certType = CertificateType.Eicr,
                    eicrExpiryDate = LocalDate(2030, 5, 6).toJavaLocalDate(),
                    eicIssueDate = null,
                    eicExpiryDate = null,
                    uploadIds = listOf(3L),
                ),
                result,
            )
        }

        @Test
        fun `maps an EIC's issue and expiry dates`() {
            // Arrange
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EIC)
            whenever(mockDetailsTask.mapElectricalCertificateTypeToGlobalCertificateType()).thenReturn(CertificateType.Eic)
            whenever(mockDetailsTask.getEicIssueDateIfReachable()).thenReturn(LocalDate(2024, 3, 20))
            // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
            whenever(mockDetailsTask.getEicExpiryDateIfReachable()).thenReturn(LocalDate(2029, 3, 20))
            whenever(mockDetailsTask.electricalUploadIds).thenReturn(listOf(4L))

            // Act
            val result = factory.fromElectricalSafetyDetailState(mockDetailsTask)

            // Assert
            assertEquals(
                ElectricalSafetyDetailsDataModel(
                    certType = CertificateType.Eic,
                    eicrExpiryDate = null,
                    eicIssueDate = LocalDate(2024, 3, 20).toJavaLocalDate(),
                    eicExpiryDate = LocalDate(2029, 3, 20).toJavaLocalDate(),
                    uploadIds = listOf(4L),
                ),
                result,
            )
        }

        @Test
        fun `throws when electrical safety is to be provided later`() {
            // Arrange
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.PROVIDE_THIS_LATER)

            // Act
            val exception = assertThrows<IllegalStateException> { factory.fromElectricalSafetyDetailState(mockDetailsTask) }

            // Assert
            assertEquals("Electrical safety can only be provided later when registering a property", exception.message)
        }
    }

    @Nested
    inner class FromEpcDetailState {
        @Mock
        private lateinit var mockDetailsTask: EpcDetailsTask

        @Mock
        private lateinit var mockHasEpcStep: HasEpcStep

        @Mock
        private lateinit var mockTenancyCheckStep: EpcInDateAtStartOfTenancyCheckStep

        @Mock
        private lateinit var mockEpcExemptionStep: EpcExemptionStep

        @Mock
        private lateinit var mockMeesExemptionStep: MeesExemptionStep

        @BeforeEach
        fun setUp() {
            whenever(mockDetailsTask.hasEpcStep).thenReturn(mockHasEpcStep)
        }

        @Test
        fun `maps the accepted EPC, tenancy check and MEES exemption`() {
            // Arrange
            val acceptedEpc = MockEpcData.createEpcDataModel()
            whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.HAS_EPC)
            whenever(mockDetailsTask.acceptedEpcIfStillAccepted).thenReturn(acceptedEpc)
            whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(acceptedEpc.certificateNumber))
                .thenReturn("https://epc.example/cert")
            stubFormSteps(
                tenancyCheckFormModel = EpcInDateAtStartOfTenancyCheckFormModel().apply { tenancyStartedBeforeExpiry = true },
                meesExemptionFormModel = MeesExemptionReasonFormModel().apply { exemptionReason = MeesExemptionReason.HIGH_COST },
            )

            // Act
            val result = factory.fromEpcDetailState(mockDetailsTask)

            // Assert
            assertEquals(
                EpcDetailsDataModel(
                    certificateUrl = "https://epc.example/cert",
                    expiryDate = acceptedEpc.expiryDateAsJavaLocalDate,
                    energyRating = acceptedEpc.energyRating,
                    tenancyStartedBeforeExpiry = true,
                    exemptionReason = null,
                    meesExemptionReason = MeesExemptionReason.HIGH_COST,
                ),
                result,
            )
        }

        @Test
        fun `maps an EPC exemption when no EPC was accepted`() {
            // Arrange
            whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.NO_EPC)
            whenever(mockDetailsTask.acceptedEpcIfStillAccepted).thenReturn(null)
            stubFormSteps(epcExemptionFormModel = EpcExemptionFormModel().apply { exemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION })

            // Act
            val result = factory.fromEpcDetailState(mockDetailsTask)

            // Assert
            assertEquals(
                EpcDetailsDataModel(
                    certificateUrl = null,
                    expiryDate = null,
                    energyRating = null,
                    tenancyStartedBeforeExpiry = null,
                    exemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION,
                    meesExemptionReason = null,
                ),
                result,
            )
        }

        @Test
        fun `throws when the EPC is to be provided later`() {
            // Arrange
            whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.PROVIDE_LATER)

            // Act
            val exception = assertThrows<IllegalStateException> { factory.fromEpcDetailState(mockDetailsTask) }

            // Assert
            assertEquals("The EPC can only be provided later when registering a property", exception.message)
        }

        private fun stubFormSteps(
            tenancyCheckFormModel: EpcInDateAtStartOfTenancyCheckFormModel? = null,
            epcExemptionFormModel: EpcExemptionFormModel? = null,
            meesExemptionFormModel: MeesExemptionReasonFormModel? = null,
        ) {
            whenever(mockDetailsTask.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyCheckStep)
            whenever(mockDetailsTask.epcExemptionStep).thenReturn(mockEpcExemptionStep)
            whenever(mockDetailsTask.meesExemptionStep).thenReturn(mockMeesExemptionStep)
            whenever(mockTenancyCheckStep.formModelIfReachableOrNull).thenReturn(tenancyCheckFormModel)
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(epcExemptionFormModel)
            whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(meesExemptionFormModel)
        }
    }
}
