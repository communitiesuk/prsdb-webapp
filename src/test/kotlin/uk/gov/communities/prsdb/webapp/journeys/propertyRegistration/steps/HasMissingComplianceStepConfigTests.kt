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
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CombinedComplianceCheckState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSupplyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel

@ExtendWith(MockitoExtension::class)
class HasMissingComplianceStepConfigTests {
    @Mock
    private lateinit var mockState: CombinedComplianceCheckState

    private val stepConfig = HasMissingComplianceStepConfig()

    @Nested
    inner class Mode {
        @Test
        fun `returns UNOCCUPIED_OR_ALL_CERTIFICATES when not occupied`() {
            // Arrange
            whenever(mockState.isOccupied).thenReturn(false)
            setupGasCertMissing()

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_VALID_CERTIFICATES, result)
        }

        @Test
        fun `returns OCCUPIED_AND_HAS_MISSING_CERTIFICATES when occupied and gas cert missing`() {
            // Arrange
            whenever(mockState.isOccupied).thenReturn(true)
            setupGasCertMissing()

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES, result)
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
            assertEquals(ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES, result)
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
            assertEquals(ConfirmMissingComplianceCheckResult.OCCUPIED_AND_HAS_INVALID_CERTIFICATES, result)
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
            assertEquals(ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_VALID_CERTIFICATES, result)
        }

        @Test
        fun `returns UNOCCUPIED_OR_VALID_CERTIFICATES when occupied but all certs are provide later`() {
            // Arrange
            whenever(mockState.isOccupied).thenReturn(true)
            setupGasCertProvideLater()
            setupElectricalCertProvideLater()
            setupEpcProvideLater()

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(ConfirmMissingComplianceCheckResult.UNOCCUPIED_OR_VALID_CERTIFICATES, result)
        }

        private fun setupGasCertMissing() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val gasFormModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(gasFormModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.hasGasCertStep).thenReturn(mock<HasGasCertStep>())
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(null)
        }

        private fun setupGasCertPresent() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val gasFormModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(gasFormModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.hasGasCertStep).thenReturn(mock<HasGasCertStep>())
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)
        }

        private fun setupElectricalCertMissing() {
            whenever(mockState.hasElectricalCertStep).thenReturn(mock<HasElectricalCertStep>())
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(null)
        }

        private fun setupElectricalCertPresent() {
            whenever(mockState.hasElectricalCertStep).thenReturn(mock<HasElectricalCertStep>())
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)
        }

        private fun setupEpcMissing() {
            whenever(mockState.hasEpcStep).thenReturn(mock<HasEpcStep>())
            whenever(mockState.acceptedEpcIfStillAccepted).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        }

        private fun setupEpcPresent() {
            whenever(mockState.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(false)
            whenever(mockEpc.isEnergyRatingEOrBetter()).thenReturn(true)
            whenever(mockState.acceptedEpcIfStillAccepted).thenReturn(mockEpc)
        }

        private fun setupGasCertProvideLater() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val gasFormModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(gasFormModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            val mockHasGasCertStep = mock<HasGasCertStep>()
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.hasGasCertStep).thenReturn(mockHasGasCertStep)
        }

        private fun setupElectricalCertProvideLater() {
            val mockHasElectricalCertStep = mock<HasElectricalCertStep>()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)
        }

        private fun setupEpcProvideLater() {
            val mockHasEpcStep = mock<HasEpcStep>()
            whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.PROVIDE_LATER)
            whenever(mockState.hasEpcStep).thenReturn(mockHasEpcStep)
        }
    }

    @Nested
    inner class IsGasCertMissingOrExpired {
        @Test
        fun `returns false when user chose provide this later`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            val mockHasGasCertStep = mock<HasGasCertStep>()
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.hasGasCertStep).thenReturn(mockHasGasCertStep)

            assertFalse(HasMissingComplianceStepConfig.isGasCertInvalid(mockState))
        }

        @Test
        fun `returns false when gas supply step not reachable`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)

            assertFalse(HasMissingComplianceStepConfig.isGasCertInvalid(mockState))
        }

        @Test
        fun `returns false when no gas supply`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = false }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)

            assertFalse(HasMissingComplianceStepConfig.isGasCertInvalid(mockState))
        }

        @Test
        fun `returns true when has gas supply and cert is missing`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.hasGasCertStep).thenReturn(mock<HasGasCertStep>())
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(null)

            assertTrue(HasMissingComplianceStepConfig.isGasCertInvalid(mockState))
        }

        @Test
        fun `returns true when has gas supply and cert is outdated`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.hasGasCertStep).thenReturn(mock<HasGasCertStep>())
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(true)

            assertTrue(HasMissingComplianceStepConfig.isGasCertInvalid(mockState))
        }

        @Test
        fun `returns false when has gas supply and cert is valid`() {
            val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
            val formModel = GasSupplyFormModel().apply { hasGasSupply = true }
            whenever(mockHasGasSupplyStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.hasGasCertStep).thenReturn(mock<HasGasCertStep>())
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)

            assertFalse(HasMissingComplianceStepConfig.isGasCertInvalid(mockState))
        }
    }

    @Nested
    inner class IsElectricalCertMissingOrExpired {
        @Test
        fun `returns false when user chose provide this later`() {
            val mockHasElectricalCertStep = mock<HasElectricalCertStep>()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)

            assertFalse(HasMissingComplianceStepConfig.isElectricalCertInvalid(mockState))
        }

        @Test
        fun `returns true when cert is missing`() {
            whenever(mockState.hasElectricalCertStep).thenReturn(mock<HasElectricalCertStep>())
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(null)

            assertTrue(HasMissingComplianceStepConfig.isElectricalCertInvalid(mockState))
        }

        @Test
        fun `returns true when cert is outdated`() {
            whenever(mockState.hasElectricalCertStep).thenReturn(mock<HasElectricalCertStep>())
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(true)

            assertTrue(HasMissingComplianceStepConfig.isElectricalCertInvalid(mockState))
        }

        @Test
        fun `returns false when cert is valid`() {
            whenever(mockState.hasElectricalCertStep).thenReturn(mock<HasElectricalCertStep>())
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)

            assertFalse(HasMissingComplianceStepConfig.isElectricalCertInvalid(mockState))
        }
    }

    @Nested
    inner class IsEpcMissingOrExpired {
        @Test
        fun `returns false when user chose provide later`() {
            val mockHasEpcStep = mock<HasEpcStep>()
            whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.PROVIDE_LATER)
            whenever(mockState.hasEpcStep).thenReturn(mockHasEpcStep)

            assertFalse(HasMissingComplianceStepConfig.isEpcInvalid(mockState))
        }

        @Test
        fun `returns false when accepted epc present and not expired and good rating`() {
            whenever(mockState.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(false)
            whenever(mockEpc.isEnergyRatingEOrBetter()).thenReturn(true)
            whenever(mockState.acceptedEpcIfStillAccepted).thenReturn(mockEpc)

            assertFalse(HasMissingComplianceStepConfig.isEpcInvalid(mockState))
        }

        @Test
        fun `returns true when accepted epc present but expired and tenancy did not start before expiry`() {
            whenever(mockState.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(true)
            whenever(mockState.acceptedEpcIfStillAccepted).thenReturn(mockEpc)
            val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
            whenever(mockTenancyStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE)
            whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)

            assertTrue(HasMissingComplianceStepConfig.isEpcInvalid(mockState))
        }

        @Test
        fun `returns false when accepted epc present but expired and tenancy started before expiry`() {
            whenever(mockState.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(true)
            whenever(mockEpc.isEnergyRatingEOrBetter()).thenReturn(true)
            whenever(mockState.acceptedEpcIfStillAccepted).thenReturn(mockEpc)
            val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
            whenever(mockTenancyStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.IN_DATE)
            whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)

            assertFalse(HasMissingComplianceStepConfig.isEpcInvalid(mockState))
        }

        @Test
        fun `returns true when accepted epc is expired with tenancy started before expiry but has low rating and no mees exemption`() {
            whenever(mockState.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(true)
            whenever(mockEpc.isEnergyRatingEOrBetter()).thenReturn(false)
            whenever(mockState.acceptedEpcIfStillAccepted).thenReturn(mockEpc)
            val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
            whenever(mockTenancyStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.IN_DATE)
            whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)
            val mockMeesExemptionStep = mock<MeesExemptionStep>()
            whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)

            assertTrue(HasMissingComplianceStepConfig.isEpcInvalid(mockState))
        }

        @Test
        fun `returns true when accepted epc has low rating and no mees exemption`() {
            whenever(mockState.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(false)
            whenever(mockEpc.isEnergyRatingEOrBetter()).thenReturn(false)
            whenever(mockState.acceptedEpcIfStillAccepted).thenReturn(mockEpc)
            val mockMeesExemptionStep = mock<MeesExemptionStep>()
            whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)

            assertTrue(HasMissingComplianceStepConfig.isEpcInvalid(mockState))
        }

        @Test
        fun `returns false when accepted epc has low rating but has mees exemption`() {
            whenever(mockState.hasEpcStep).thenReturn(mock<HasEpcStep>())
            val mockEpc = mock<EpcDataModel>()
            whenever(mockEpc.isPastExpiryDate()).thenReturn(false)
            whenever(mockEpc.isEnergyRatingEOrBetter()).thenReturn(false)
            whenever(mockState.acceptedEpcIfStillAccepted).thenReturn(mockEpc)
            val mockMeesExemptionStep = mock<MeesExemptionStep>()
            val formModel =
                MeesExemptionReasonFormModel().apply {
                    exemptionReason = MeesExemptionReason.ALL_IMPROVEMENTS_MADE
                }
            whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)

            assertFalse(HasMissingComplianceStepConfig.isEpcInvalid(mockState))
        }

        @Test
        fun `returns true when no accepted epc and no exemption`() {
            whenever(mockState.hasEpcStep).thenReturn(mock<HasEpcStep>())
            whenever(mockState.acceptedEpcIfStillAccepted).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)

            assertTrue(HasMissingComplianceStepConfig.isEpcInvalid(mockState))
        }

        @Test
        fun `returns false when no accepted epc but exemption present`() {
            whenever(mockState.hasEpcStep).thenReturn(mock<HasEpcStep>())
            whenever(mockState.acceptedEpcIfStillAccepted).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            val formModel =
                EpcExemptionFormModel().apply {
                    exemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT
                }
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)

            assertFalse(HasMissingComplianceStepConfig.isEpcInvalid(mockState))
        }

        @Test
        fun `returns true when no accepted epc and exemption reason is null`() {
            whenever(mockState.hasEpcStep).thenReturn(mock<HasEpcStep>())
            whenever(mockState.acceptedEpcIfStillAccepted).thenReturn(null)
            val mockEpcExemptionStep = mock<EpcExemptionStep>()
            val formModel = EpcExemptionFormModel()
            whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(formModel)
            whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)

            assertTrue(HasMissingComplianceStepConfig.isEpcInvalid(mockState))
        }
    }
}
