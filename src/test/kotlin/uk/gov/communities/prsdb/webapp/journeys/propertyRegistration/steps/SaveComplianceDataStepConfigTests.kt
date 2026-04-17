package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
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
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class SaveComplianceDataStepConfigTests {
    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    private lateinit var stepConfig: SaveComplianceDataStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig =
            SaveComplianceDataStepConfig(
                propertyComplianceService = mockPropertyComplianceService,
                epcCertificateUrlProvider = mockEpcCertificateUrlProvider,
            )
    }

    @Test
    fun `mode returns COMPLETE`() {
        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Nested
    inner class AfterStepIsReached {
        @Test
        fun `does nothing when registrationNumberValue is null`() {
            whenever(mockState.registrationNumberValue).thenReturn(null)

            stepConfig.afterStepIsReached(mockState)

            verifyNoInteractions(mockPropertyComplianceService)
        }

        @Test
        fun `calls service with compliance data from state`() {
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

            stepConfig.afterStepIsReached(mockState)

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
        fun `passes null for unreachable steps`() {
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

            stepConfig.afterStepIsReached(mockState)

            verify(mockPropertyComplianceService).saveRegistrationComplianceData(
                registrationNumberValue = registrationNumberValue,
            )
        }
    }

    @Nested
    inner class ResolveNextDestination {
        @Test
        fun `deletes journey and returns default destination`() {
            val defaultDestination = Destination.ExternalUrl("redirect")

            val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

            verify(mockState).deleteJourney()
            assertEquals(defaultDestination, result)
        }
    }
}
