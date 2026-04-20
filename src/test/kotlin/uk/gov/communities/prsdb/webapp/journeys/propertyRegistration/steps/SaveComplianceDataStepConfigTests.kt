package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcInDateAtStartOfTenancyCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService

@ExtendWith(MockitoExtension::class)
class SaveComplianceDataStepConfigTests {
    @Mock
    lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    @Mock
    lateinit var mockState: PropertyRegistrationJourneyState

    @Test
    fun `mode returns COMPLETE`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `afterStepIsReached throws when registrationNumberValue is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.registrationNumberValue).thenReturn(null)

        // Act & Assert
        assertThrows<IllegalStateException> {
            stepConfig.afterStepIsReached(mockState)
        }
    }

    @Test
    fun `afterStepIsReached calls service with compliance data from state`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val registrationNumberValue = 12345L
        val gasCertIssueDate = LocalDate(2024, 6, 15)
        val eicrExpiryDate = LocalDate(2029, 3, 20)
        val certificateNumber = "1234-5678-9012-3456-7890"
        val epcUrl = "https://epc.example.com/$certificateNumber"

        val epcDataModel =
            EpcDataModel(
                certificateNumber = certificateNumber,
                singleLineAddress = "1 Test St",
                energyRating = "B",
                expiryDate = LocalDate(2030, 1, 1),
            )

        val tenancyFormModel =
            EpcInDateAtStartOfTenancyCheckFormModel().apply {
                tenancyStartedBeforeExpiry = true
            }
        val epcExemptionFormModel =
            EpcExemptionFormModel().apply {
                exemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT
            }
        val meesExemptionFormModel =
            MeesExemptionReasonFormModel().apply {
                exemptionReason = MeesExemptionReason.HIGH_COST
            }

        val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
        val mockEpcExemptionStep = mock<EpcExemptionStep>()
        val mockMeesExemptionStep = mock<MeesExemptionStep>()

        whenever(mockState.registrationNumberValue).thenReturn(registrationNumberValue)
        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(gasCertIssueDate)
        whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(eicrExpiryDate)
        whenever(mockState.acceptedEpc).thenReturn(epcDataModel)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(certificateNumber)).thenReturn(epcUrl)
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)
        whenever(mockTenancyStep.formModelIfReachableOrNull).thenReturn(tenancyFormModel)
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(epcExemptionFormModel)
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(meesExemptionFormModel)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyComplianceService).saveRegistrationComplianceData(
            registrationNumberValue = registrationNumberValue,
            gasSafetyCertIssueDate = java.time.LocalDate.of(2024, 6, 15),
            eicrExpiryDate = java.time.LocalDate.of(2029, 3, 20),
            epcCertificateUrl = epcUrl,
            epcExpiryDate = java.time.LocalDate.of(2030, 1, 1),
            epcEnergyRating = "B",
            tenancyStartedBeforeEpcExpiry = true,
            epcExemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT,
            epcMeesExemptionReason = MeesExemptionReason.HIGH_COST,
        )
    }

    @Test
    fun `afterStepIsReached passes null for unreachable steps`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val registrationNumberValue = 12345L

        val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
        val mockEpcExemptionStep = mock<EpcExemptionStep>()
        val mockMeesExemptionStep = mock<MeesExemptionStep>()

        whenever(mockState.registrationNumberValue).thenReturn(registrationNumberValue)
        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(null)
        whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(null)
        whenever(mockState.acceptedEpc).thenReturn(null)
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)
        whenever(mockTenancyStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(null)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyComplianceService).saveRegistrationComplianceData(
            registrationNumberValue = registrationNumberValue,
        )
    }

    @Test
    fun `resolveNextDestination deletes journey and returns default destination`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val defaultDestination = Destination.ExternalUrl("redirect")

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        verify(mockState).deleteJourney()
        assertEquals(defaultDestination, result)
    }

    private fun setupStepConfig(): SaveComplianceDataStepConfig =
        SaveComplianceDataStepConfig(
            propertyComplianceService = mockPropertyComplianceService,
            epcCertificateUrlProvider = mockEpcCertificateUrlProvider,
        )
}
