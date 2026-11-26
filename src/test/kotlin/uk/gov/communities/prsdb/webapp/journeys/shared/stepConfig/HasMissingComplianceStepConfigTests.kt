package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckMode
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
import uk.gov.communities.prsdb.webapp.journeys.shared.states.MissingComplianceState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel

@ExtendWith(MockitoExtension::class)
class HasMissingComplianceStepConfigTests {
    @Mock
    private lateinit var mockState: MissingComplianceState

    private lateinit var stepConfig: HasMissingComplianceStepConfig

    @BeforeEach
    fun setUpStepConfig() {
        stepConfig = HasMissingComplianceStepConfig()
    }

    @Nested
    inner class Mode {
        @Test
        fun `returns UNOCCUPIED_OR_VALID_CERTIFICATES_OR_DELEGATED when the check is not applicable`() {
            // Arrange
            whenever(mockState.isMissingComplianceCheckApplicable).thenReturn(false)

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_VALID_CERTIFICATES_OR_DELEGATED, result)
        }

        @Test
        fun `returns OCCUPIED_AND_HAS_INVALID_CERTIFICATES when applicable and the gas cert is missing`() {
            // Arrange
            whenever(mockState.isMissingComplianceCheckApplicable).thenReturn(true)
            whenever(mockState.isGasCertMissing).thenReturn(true)

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES, result)
        }

        @Test
        fun `returns OCCUPIED_AND_HAS_INVALID_CERTIFICATES when applicable and only the electrical cert is missing`() {
            // Arrange
            whenever(mockState.isMissingComplianceCheckApplicable).thenReturn(true)
            whenever(mockState.isGasCertMissing).thenReturn(false)
            whenever(mockState.isElectricalCertMissing).thenReturn(true)

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES, result)
        }

        @Test
        fun `returns OCCUPIED_AND_HAS_INVALID_CERTIFICATES when applicable and only the EPC is missing`() {
            // Arrange
            whenever(mockState.isMissingComplianceCheckApplicable).thenReturn(true)
            whenever(mockState.isGasCertMissing).thenReturn(false)
            whenever(mockState.isElectricalCertMissing).thenReturn(false)
            whenever(mockState.isEpcMissing).thenReturn(true)

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES, result)
        }

        @Test
        fun `returns UNOCCUPIED_OR_VALID_CERTIFICATES_OR_DELEGATED when applicable and nothing is missing`() {
            // Arrange
            whenever(mockState.isMissingComplianceCheckApplicable).thenReturn(true)
            whenever(mockState.isGasCertMissing).thenReturn(false)
            whenever(mockState.isElectricalCertMissing).thenReturn(false)
            whenever(mockState.isEpcMissing).thenReturn(false)

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_VALID_CERTIFICATES_OR_DELEGATED, result)
        }
    }

    @Nested
    inner class IsGasCertMissingOrExpired {
        @Mock
        lateinit var mockGasSafetyTask: GasSafetyTask

        @Mock
        lateinit var mockGasSafetyDetailTask: GasSafetyDetailsTask

        @BeforeEach
        fun setUp() {
            whenever(mockGasSafetyTask.gasSafetyDetailsTask).thenReturn(mockGasSafetyDetailTask)
        }

        @Test
        fun `returns false when user chose provide this later`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(HasGasSupplyMode.PROVIDE_LATER)
            whenever(mockGasSafetyDetailTask.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)

            assertFalse(HasMissingComplianceStepConfig.isGasCertInvalid(mockGasSafetyTask))
        }

        @Test
        fun `returns false when gas supply step not reachable`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(null)
            whenever(mockGasSafetyDetailTask.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)

            assertFalse(HasMissingComplianceStepConfig.isGasCertInvalid(mockGasSafetyTask))
        }

        @Test
        fun `returns false when no gas supply`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(HasGasSupplyMode.NO_SUPPLY)
            whenever(mockGasSafetyDetailTask.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)

            assertFalse(HasMissingComplianceStepConfig.isGasCertInvalid(mockGasSafetyTask))
        }

        @Test
        fun `returns true when has gas supply and cert is missing`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(HasGasSupplyMode.HAS_SUPPLY)
            whenever(mockGasSafetyDetailTask.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockGasSafetyDetailTask.getGasSafetyCertificateIsOutdated()).thenReturn(null)

            assertTrue(HasMissingComplianceStepConfig.isGasCertInvalid(mockGasSafetyTask))
        }

        @Test
        fun `returns true when has gas supply and cert is outdated`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(HasGasSupplyMode.HAS_SUPPLY)
            whenever(mockGasSafetyDetailTask.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockGasSafetyDetailTask.getGasSafetyCertificateIsOutdated()).thenReturn(true)

            assertTrue(HasMissingComplianceStepConfig.isGasCertInvalid(mockGasSafetyTask))
        }

        @Test
        fun `returns false when has gas supply and cert is valid`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(HasGasSupplyMode.HAS_SUPPLY)
            whenever(mockGasSafetyDetailTask.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockGasSafetyDetailTask.getGasSafetyCertificateIsOutdated()).thenReturn(false)

            assertFalse(HasMissingComplianceStepConfig.isGasCertInvalid(mockGasSafetyTask))
        }
    }

    @Nested
    inner class IsElectricalCertMissingOrExpired {
        @Mock
        lateinit var mockElectricalSafetyTask: ElectricalSafetyTask

        @Mock
        lateinit var mockElectricalSafetyDetailTask: ElectricalSafetyDetailsTask

        @BeforeEach
        fun setUp() {
            whenever(mockElectricalSafetyTask.electricalSafetyDetailsTask).thenReturn(mockElectricalSafetyDetailTask)
        }

        @Test
        fun `returns false when user chose provide this later`() {
            val mockHasElectricalCertStep = mock<HasElectricalCertStep>()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.PROVIDE_THIS_LATER)
            whenever(mockElectricalSafetyDetailTask.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)

            assertFalse(HasMissingComplianceStepConfig.isElectricalCertInvalid(mockElectricalSafetyTask))
        }

        @Test
        fun `returns true when cert is missing`() {
            whenever(mockElectricalSafetyDetailTask.hasElectricalCertStep).thenReturn(mock<HasElectricalCertStep>())
            whenever(mockElectricalSafetyDetailTask.getElectricalCertificateIsOutdated()).thenReturn(null)

            assertTrue(HasMissingComplianceStepConfig.isElectricalCertInvalid(mockElectricalSafetyTask))
        }

        @Test
        fun `returns true when cert is outdated`() {
            whenever(mockElectricalSafetyDetailTask.hasElectricalCertStep).thenReturn(mock<HasElectricalCertStep>())
            whenever(mockElectricalSafetyDetailTask.getElectricalCertificateIsOutdated()).thenReturn(true)

            assertTrue(HasMissingComplianceStepConfig.isElectricalCertInvalid(mockElectricalSafetyTask))
        }

        @Test
        fun `returns false when cert is valid`() {
            whenever(mockElectricalSafetyDetailTask.hasElectricalCertStep).thenReturn(mock<HasElectricalCertStep>())
            whenever(mockElectricalSafetyDetailTask.getElectricalCertificateIsOutdated()).thenReturn(false)

            assertFalse(HasMissingComplianceStepConfig.isElectricalCertInvalid(mockElectricalSafetyTask))
        }
    }

    @Nested
    inner class IsEpcMissingOrExpired {
        @Mock
        lateinit var mockEpcTask: EpcTask

        @Mock
        lateinit var mockEpcDetailsTask: EpcDetailsTask

        @BeforeEach
        fun setUp() {
            whenever(mockEpcTask.epcDetailsTask).thenReturn(mockEpcDetailsTask)
        }

        @Test
        fun `returns false when user chose provide later`() {
            val mockHasEpcStep = mock<HasEpcStep>()
            whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.PROVIDE_LATER)
            whenever(mockEpcDetailsTask.hasEpcStep).thenReturn(mockHasEpcStep)

            assertFalse(HasMissingComplianceStepConfig.isEpcInvalid(mockEpcTask))
        }

        @Test
        fun `returns false when accepted epc present and not expired and good rating`() {
            whenever(mockEpcDetailsTask.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(false)
            whenever(mockEpc.isEnergyRatingEOrBetter()).thenReturn(true)
            whenever(mockEpcDetailsTask.acceptedEpcIfStillAccepted).thenReturn(mockEpc)

            assertFalse(HasMissingComplianceStepConfig.isEpcInvalid(mockEpcTask))
        }

        @Test
        fun `returns true when accepted epc present but expired and tenancy did not start before expiry`() {
            whenever(mockEpcDetailsTask.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(true)
            whenever(mockEpcDetailsTask.acceptedEpcIfStillAccepted).thenReturn(mockEpc)
            val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
            whenever(mockTenancyStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE)
            whenever(mockEpcDetailsTask.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)

            assertTrue(HasMissingComplianceStepConfig.isEpcInvalid(mockEpcTask))
        }

        @Test
        fun `returns false when accepted epc present but expired and tenancy started before expiry`() {
            whenever(mockEpcDetailsTask.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(true)
            whenever(mockEpc.isEnergyRatingEOrBetter()).thenReturn(true)
            whenever(mockEpcDetailsTask.acceptedEpcIfStillAccepted).thenReturn(mockEpc)
            val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
            whenever(mockTenancyStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.IN_DATE)
            whenever(mockEpcDetailsTask.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)

            assertFalse(HasMissingComplianceStepConfig.isEpcInvalid(mockEpcTask))
        }

        @Test
        fun `returns true when accepted epc is expired with tenancy started before expiry but has low rating and no mees exemption`() {
            whenever(mockEpcDetailsTask.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(true)
            whenever(mockEpc.isEnergyRatingEOrBetter()).thenReturn(false)
            whenever(mockEpcDetailsTask.acceptedEpcIfStillAccepted).thenReturn(mockEpc)
            val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
            whenever(mockTenancyStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.IN_DATE)
            whenever(mockEpcDetailsTask.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)
            val mockMeesExemptionStep = mock<MeesExemptionStep>()
            whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockEpcDetailsTask.meesExemptionStep).thenReturn(mockMeesExemptionStep)

            assertTrue(HasMissingComplianceStepConfig.isEpcInvalid(mockEpcTask))
        }

        @Test
        fun `returns true when accepted epc has low rating and no mees exemption`() {
            whenever(mockEpcDetailsTask.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(false)
            whenever(mockEpc.isEnergyRatingEOrBetter()).thenReturn(false)
            whenever(mockEpcDetailsTask.acceptedEpcIfStillAccepted).thenReturn(mockEpc)
            val mockMeesExemptionStep = mock<MeesExemptionStep>()
            whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockEpcDetailsTask.meesExemptionStep).thenReturn(mockMeesExemptionStep)

            assertTrue(HasMissingComplianceStepConfig.isEpcInvalid(mockEpcTask))
        }

        @Test
        fun `returns false when accepted epc has low rating but has mees exemption`() {
            whenever(mockEpcDetailsTask.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(false)
            whenever(mockEpc.isEnergyRatingEOrBetter()).thenReturn(false)
            whenever(mockEpcDetailsTask.acceptedEpcIfStillAccepted).thenReturn(mockEpc)
            val mockMeesExemptionStep = mock<MeesExemptionStep>()
            val formModel =
                MeesExemptionReasonFormModel().apply {
                    exemptionReason = MeesExemptionReason.ALL_IMPROVEMENTS_MADE
                }
            whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockEpcDetailsTask.meesExemptionStep).thenReturn(mockMeesExemptionStep)

            assertFalse(HasMissingComplianceStepConfig.isEpcInvalid(mockEpcTask))
        }

        @Test
        fun `returns true when no accepted epc and no exemption`() {
            whenever(mockEpcDetailsTask.hasEpcStep).thenReturn(mock<HasEpcStep>())
            whenever(mockEpcDetailsTask.acceptedEpcIfStillAccepted).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockEpcDetailsTask.epcExemptionStep).thenReturn(mockEpcExemptionStep)

            assertTrue(HasMissingComplianceStepConfig.isEpcInvalid(mockEpcTask))
        }

        @Test
        fun `returns false when no accepted epc but exemption present`() {
            whenever(mockEpcDetailsTask.hasEpcStep).thenReturn(mock<HasEpcStep>())
            whenever(mockEpcDetailsTask.acceptedEpcIfStillAccepted).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            val formModel =
                EpcExemptionFormModel().apply {
                    exemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT
                }
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockEpcDetailsTask.epcExemptionStep).thenReturn(mockEpcExemptionStep)

            assertFalse(HasMissingComplianceStepConfig.isEpcInvalid(mockEpcTask))
        }

        @Test
        fun `returns true when no accepted epc and exemption reason is null`() {
            whenever(mockEpcDetailsTask.hasEpcStep).thenReturn(mock<HasEpcStep>())
            whenever(mockEpcDetailsTask.acceptedEpcIfStillAccepted).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            val formModel = EpcExemptionFormModel()
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockEpcDetailsTask.epcExemptionStep).thenReturn(mockEpcExemptionStep)

            assertTrue(HasMissingComplianceStepConfig.isEpcInvalid(mockEpcTask))
        }
    }
}
