package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.LocalDate
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
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSupplyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel

@ExtendWith(MockitoExtension::class)
class ConfirmMissingComplianceCheckStepConfigTests {
    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    private val stepConfig = ConfirmMissingComplianceCheckStepConfig()

    @Test
    fun `mode returns COMPLETE`() {
        val result = stepConfig.mode(mockState)
        assertEquals(Complete.COMPLETE, result)
    }

    @Nested
    inner class ResolveNextDestination {
        private val defaultDestination = Destination.ExternalUrl("default")

        @Test
        fun `returns confirm step when occupied and gas cert missing`() {
            // Arrange
            setupOccupied(true)
            setupGasCertMissing()
            setupElectricalCertPresent()
            setupEpcPresent()
            val mockConfirmStep = mock<ConfirmMissingComplianceStep>()
            whenever(mockConfirmStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.confirmMissingComplianceStep).thenReturn(mockConfirmStep)

            // Act
            val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

            // Assert
            assertTrue(result is Destination.VisitableStep)
        }

        @Test
        fun `returns confirm step when occupied and electrical cert missing`() {
            // Arrange
            setupOccupied(true)
            setupGasCertPresent()
            setupElectricalCertMissing()
            setupEpcPresent()
            val mockConfirmStep = mock<ConfirmMissingComplianceStep>()
            whenever(mockConfirmStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.confirmMissingComplianceStep).thenReturn(mockConfirmStep)

            // Act
            val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

            // Assert
            assertTrue(result is Destination.VisitableStep)
        }

        @Test
        fun `returns confirm step when occupied and epc missing`() {
            // Arrange
            setupOccupied(true)
            setupGasCertPresent()
            setupElectricalCertPresent()
            setupEpcMissing()
            val mockConfirmStep = mock<ConfirmMissingComplianceStep>()
            whenever(mockConfirmStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.confirmMissingComplianceStep).thenReturn(mockConfirmStep)

            // Act
            val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

            // Assert
            assertTrue(result is Destination.VisitableStep)
        }

        @Test
        fun `returns default when not occupied`() {
            // Arrange
            setupOccupied(false)

            // Act
            val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

            // Assert
            assertEquals(defaultDestination, result)
        }

        @Test
        fun `returns default when occupied is null`() {
            // Arrange
            val mockOccupiedStep = mock<OccupiedStep>()
            whenever(mockOccupiedStep.formModelOrNull).thenReturn(null)
            whenever(mockState.occupied).thenReturn(mockOccupiedStep)

            // Act
            val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

            // Assert
            assertEquals(defaultDestination, result)
        }

        @Test
        fun `returns default when occupied but all certs present`() {
            // Arrange
            setupOccupied(true)
            setupGasCertPresent()
            setupElectricalCertPresent()
            setupEpcPresent()

            // Act
            val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

            // Assert
            assertEquals(defaultDestination, result)
        }

        private fun setupOccupied(isOccupied: Boolean) {
            val mockOccupiedStep = mock<OccupiedStep>()
            val formModel = OccupancyFormModel().apply { occupied = isOccupied }
            whenever(mockOccupiedStep.formModelOrNull).thenReturn(formModel)
            whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        }

        private fun setupGasCertMissing() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val gasFormModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(gasFormModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(null)
        }

        private fun setupGasCertPresent() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val gasFormModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(gasFormModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(LocalDate(2025, 1, 1))
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)
        }

        private fun setupElectricalCertMissing() {
            whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(null)
        }

        private fun setupElectricalCertPresent() {
            whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(LocalDate(2030, 1, 1))
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)
        }

        private fun setupEpcMissing() {
            whenever(mockState.acceptedEpc).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        }

        private fun setupEpcPresent() {
            whenever(mockState.acceptedEpc).thenReturn(mock<EpcDataModel>())
        }
    }

    @Nested
    inner class IsGasCertMissing {
        @Test
        fun `returns false when gas supply step not reachable`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)

            assertFalse(ConfirmMissingComplianceCheckStepConfig.isGasCertMissing(mockState))
        }

        @Test
        fun `returns false when no gas supply`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = false }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)

            assertFalse(ConfirmMissingComplianceCheckStepConfig.isGasCertMissing(mockState))
        }

        @Test
        fun `returns true when has gas supply and no issue date`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(null)

            assertTrue(ConfirmMissingComplianceCheckStepConfig.isGasCertMissing(mockState))
        }

        @Test
        fun `returns true when has gas supply and cert is outdated`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(LocalDate(2020, 1, 1))
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(true)

            assertTrue(ConfirmMissingComplianceCheckStepConfig.isGasCertMissing(mockState))
        }

        @Test
        fun `returns false when has gas supply and cert is valid`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(LocalDate(2025, 1, 1))
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)

            assertFalse(ConfirmMissingComplianceCheckStepConfig.isGasCertMissing(mockState))
        }
    }

    @Nested
    inner class IsElectricalCertMissing {
        @Test
        fun `returns true when no expiry date`() {
            whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(null)

            assertTrue(ConfirmMissingComplianceCheckStepConfig.isElectricalCertMissing(mockState))
        }

        @Test
        fun `returns true when cert is outdated`() {
            whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(LocalDate(2020, 1, 1))
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(true)

            assertTrue(ConfirmMissingComplianceCheckStepConfig.isElectricalCertMissing(mockState))
        }

        @Test
        fun `returns false when cert is valid`() {
            whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(LocalDate(2030, 1, 1))
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)

            assertFalse(ConfirmMissingComplianceCheckStepConfig.isElectricalCertMissing(mockState))
        }
    }

    @Nested
    inner class IsEpcMissing {
        @Test
        fun `returns false when accepted epc present`() {
            whenever(mockState.acceptedEpc).thenReturn(mock<EpcDataModel>())

            assertFalse(ConfirmMissingComplianceCheckStepConfig.isEpcMissing(mockState))
        }

        @Test
        fun `returns true when no accepted epc and no exemption`() {
            whenever(mockState.acceptedEpc).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)

            assertTrue(ConfirmMissingComplianceCheckStepConfig.isEpcMissing(mockState))
        }

        @Test
        fun `returns false when no accepted epc but exemption present`() {
            whenever(mockState.acceptedEpc).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            val formModel = EpcExemptionFormModel().apply { exemptionReason = EpcExemptionReason.LISTED_BUILDING }
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)

            assertFalse(ConfirmMissingComplianceCheckStepConfig.isEpcMissing(mockState))
        }

        @Test
        fun `returns true when no accepted epc and exemption reason is null`() {
            whenever(mockState.acceptedEpc).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            val formModel = EpcExemptionFormModel()
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)

            assertTrue(ConfirmMissingComplianceCheckStepConfig.isEpcMissing(mockState))
        }
    }
}
