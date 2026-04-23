package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CombinedComplianceCheckState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSupplyFormModel

@ExtendWith(MockitoExtension::class)
class ConfirmMissingComplianceCheckStepConfigTests {
    @Mock
    private lateinit var mockState: CombinedComplianceCheckState

    private val stepConfig = ConfirmMissingComplianceCheckStepConfig()

    @Nested
    inner class Mode {
        @Test
        fun `returns UNOCCUPIED_OR_ALL_CERTIFICATES when not occupied`() {
            // Arrange
            whenever(mockState.isOccupied).thenReturn(false)

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_ALL_CERTIFICATES, result)
        }

        @Test
        fun `returns OCCUPIED_AND_HAS_MISSING_CERTIFICATES when occupied and gas cert missing`() {
            // Arrange
            whenever(mockState.isOccupied).thenReturn(true)
            setupGasCertMissing()

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_MISSING_CERTIFICATES, result)
        }

        @Test
        fun `returns OCCUPIED_AND_HAS_MISSING_CERTIFICATES when occupied and electrical cert missing`() {
            // Arrange
            whenever(mockState.isOccupied).thenReturn(true)
            setupGasCertPresent()
            setupElectricalCertMissing()

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_MISSING_CERTIFICATES, result)
        }

        @Test
        fun `returns OCCUPIED_AND_HAS_MISSING_CERTIFICATES when occupied and epc missing`() {
            // Arrange
            whenever(mockState.isOccupied).thenReturn(true)
            setupGasCertPresent()
            setupElectricalCertPresent()
            setupEpcMissing()

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_MISSING_CERTIFICATES, result)
        }

        @Test
        fun `returns UNOCCUPIED_OR_ALL_CERTIFICATES when occupied and all certs present`() {
            // Arrange
            whenever(mockState.isOccupied).thenReturn(true)
            setupGasCertPresent()
            setupElectricalCertPresent()
            setupEpcPresent()

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_ALL_CERTIFICATES, result)
        }

        private fun setupGasCertMissing() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val gasFormModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(gasFormModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(null)
        }

        private fun setupGasCertPresent() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val gasFormModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(gasFormModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)
        }

        private fun setupElectricalCertMissing() {
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(null)
        }

        private fun setupElectricalCertPresent() {
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)
        }

        private fun setupEpcMissing() {
            whenever(mockState.acceptedEpcIfReachable).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        }

        private fun setupEpcPresent() {
            whenever(mockState.acceptedEpcIfReachable).thenReturn(mock<EpcDataModel>())
        }
    }

    @Nested
    inner class IsGasCertMissingOrExpired {
        @Test
        fun `returns false when gas supply step not reachable`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)

            assertFalse(ConfirmMissingComplianceCheckStepConfig.isGasCertMissingOrExpired(mockState))
        }

        @Test
        fun `returns false when no gas supply`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = false }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)

            assertFalse(ConfirmMissingComplianceCheckStepConfig.isGasCertMissingOrExpired(mockState))
        }

        @Test
        fun `returns true when has gas supply and cert is missing`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(null)

            assertTrue(ConfirmMissingComplianceCheckStepConfig.isGasCertMissingOrExpired(mockState))
        }

        @Test
        fun `returns true when has gas supply and cert is outdated`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(true)

            assertTrue(ConfirmMissingComplianceCheckStepConfig.isGasCertMissingOrExpired(mockState))
        }

        @Test
        fun `returns false when has gas supply and cert is valid`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)

            assertFalse(ConfirmMissingComplianceCheckStepConfig.isGasCertMissingOrExpired(mockState))
        }
    }

    @Nested
    inner class IsElectricalCertMissingOrExpired {
        @Test
        fun `returns true when cert is missing`() {
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(null)

            assertTrue(ConfirmMissingComplianceCheckStepConfig.isElectricalCertMissingOrExpired(mockState))
        }

        @Test
        fun `returns true when cert is outdated`() {
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(true)

            assertTrue(ConfirmMissingComplianceCheckStepConfig.isElectricalCertMissingOrExpired(mockState))
        }

        @Test
        fun `returns false when cert is valid`() {
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)

            assertFalse(ConfirmMissingComplianceCheckStepConfig.isElectricalCertMissingOrExpired(mockState))
        }
    }

    @Nested
    inner class IsEpcMissing {
        @Test
        fun `returns false when accepted epc present`() {
            whenever(mockState.acceptedEpcIfReachable).thenReturn(mock<EpcDataModel>())

            assertFalse(ConfirmMissingComplianceCheckStepConfig.isEpcMissing(mockState))
        }

        @Test
        fun `returns true when no accepted epc and no exemption`() {
            whenever(mockState.acceptedEpcIfReachable).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)

            assertTrue(ConfirmMissingComplianceCheckStepConfig.isEpcMissing(mockState))
        }

        @Test
        fun `returns false when no accepted epc but exemption present`() {
            whenever(mockState.acceptedEpcIfReachable).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            val formModel =
                EpcExemptionFormModel().apply {
                    exemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT
                }
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)

            assertFalse(ConfirmMissingComplianceCheckStepConfig.isEpcMissing(mockState))
        }

        @Test
        fun `returns true when no accepted epc and exemption reason is null`() {
            whenever(mockState.acceptedEpcIfReachable).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            val formModel = EpcExemptionFormModel()
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)

            assertTrue(ConfirmMissingComplianceCheckStepConfig.isEpcMissing(mockState))
        }
    }
}
