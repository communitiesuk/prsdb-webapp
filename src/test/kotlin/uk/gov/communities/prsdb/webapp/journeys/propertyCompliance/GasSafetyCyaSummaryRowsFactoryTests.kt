package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyMode
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
import java.time.LocalDate
import kotlin.test.assertEquals

class GasSafetyCyaSummaryRowsFactoryTests {
    private val mockUploadService: UploadService = mock()

    private val mockGasSafetyStep: GasSafetyStep = mock()
    private val mockGasSafetyIssueDateStep: GasSafetyIssueDateStep = mock()
    private val mockGasSafetyEngineerNumberStep: GasSafetyEngineerNumberStep = mock()
    private val mockGasSafetyUploadConfirmationStep: GasSafetyUploadConfirmationStep = mock()
    private val mockGasSafetyOutdatedStep: GasSafetyOutdatedStep = mock()
    private val mockGasSafetyExemptionStep: GasSafetyExemptionStep = mock()
    private val mockGasSafetyExemptionReasonStep: GasSafetyExemptionReasonStep = mock()
    private val mockGasSafetyExemptionOtherReasonStep: GasSafetyExemptionOtherReasonStep = mock()
    private val mockGasSafetyExemptionConfirmationStep: GasSafetyExemptionConfirmationStep = mock()
    private val mockGasSafetyExemptionMissingStep: GasSafetyExemptionMissingStep = mock()
    private val mockState: GasSafetyState = mock()

    private val childJourneyId = "child-journey-123"
    private val gasSafetyStartingStep = Destination.VisitableStep(mockGasSafetyStep, childJourneyId)
    private val changeExemptionStep = Destination.VisitableStep(mockGasSafetyExemptionStep, childJourneyId)

    private val gasFileUploadId = 123L
    private val gasSafetyFileUpload = MockPropertyComplianceData.createFileUpload(gasFileUploadId)
    private val validGasSafetyIssueDate = LocalDate.now().minusDays(5)
    private val expiredGasSafetyIssueDate = LocalDate.now().minusYears((GAS_SAFETY_CERT_VALIDITY_YEARS + 1).toLong())
    private val gasEngineerNumber = "1234567"

    @BeforeEach
    fun setupMocks() {
        whenever(mockState.gasSafetyUploadConfirmationStep).thenReturn(mockGasSafetyUploadConfirmationStep)
        whenever(mockState.gasSafetyExemptionConfirmationStep).thenReturn(mockGasSafetyExemptionConfirmationStep)
        whenever(mockState.gasSafetyExemptionMissingStep).thenReturn(mockGasSafetyExemptionMissingStep)
        whenever(mockState.gasSafetyOutdatedStep).thenReturn(mockGasSafetyOutdatedStep)
    }

    @Test
    fun `createRows returns correct summary rows when in-date certificate has been provided`() {
        // Arrange
        val propertyHasCertificate = true
        val downloadUrl = "https://example.com/gas-cert.pdf"

        whenever(mockState.gasSafetyStep).thenReturn(mockGasSafetyStep)
        whenever(mockGasSafetyStep.outcome).thenReturn(GasSafetyMode.HAS_CERTIFICATE)

        whenever(mockState.gasSafetyIssueDateStep).thenReturn(mockGasSafetyIssueDateStep)
        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(validGasSafetyIssueDate.toKotlinLocalDate())
        whenever(mockState.getGasSafetyExpiryDate())
            .thenReturn(validGasSafetyIssueDate.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong()).toKotlinLocalDate())

        whenever(mockState.gasSafetyEngineerNumberStep).thenReturn(mockGasSafetyEngineerNumberStep)
        val engineerFormModel = GasSafeEngineerNumFormModel().apply { engineerNumber = gasEngineerNumber }
        whenever(mockGasSafetyEngineerNumberStep.formModelIfReachableOrNull).thenReturn(engineerFormModel)

        whenever(mockState.getGasSafetyCertificateFileUploadIdIfReachable()).thenReturn(gasFileUploadId)
        whenever(mockUploadService.getFileUploadById(gasFileUploadId)).thenReturn(gasSafetyFileUpload)
        whenever(mockUploadService.getDownloadUrl(gasSafetyFileUpload, "gas_safety_certificate.pdf"))
            .thenReturn(downloadUrl)
        whenever(mockGasSafetyUploadConfirmationStep.outcome).thenReturn(Complete.COMPLETE)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.gasSafety.certificate",
                    "forms.checkComplianceAnswers.gasSafety.download",
                    Destination.VisitableStep(mockState.gasSafetyStep, childJourneyId),
                    downloadUrl,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.issueDate",
                    validGasSafetyIssueDate.toKotlinLocalDate(),
                    Destination.VisitableStep(mockState.gasSafetyIssueDateStep, childJourneyId),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.validUntil",
                    validGasSafetyIssueDate.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong()).toKotlinLocalDate(),
                    Destination.Nowhere(),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.gasSafety.engineerNumber",
                    gasEngineerNumber,
                    Destination.VisitableStep(mockState.gasSafetyEngineerNumberStep, childJourneyId),
                ),
            )

        // Act
        val summaryRows =
            GasSafetyCyaSummaryRowsFactory(
                propertyHasCertificate,
                gasSafetyStartingStep,
                changeExemptionStep,
                mockUploadService,
                mockState,
                childJourneyId,
            ).createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }

    @Test
    fun `createRows returns correct rows when expired certificate has been provided`() {
        // Arrange
        val propertyHasCertificate = true

        whenever(mockState.gasSafetyIssueDateStep).thenReturn(mockGasSafetyIssueDateStep)
        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(expiredGasSafetyIssueDate.toKotlinLocalDate())
        whenever(mockState.getGasSafetyExpiryDate())
            .thenReturn(expiredGasSafetyIssueDate.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong()).toKotlinLocalDate())

        whenever(mockGasSafetyOutdatedStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockState.gasSafetyEngineerNumberStep).thenReturn(mockGasSafetyEngineerNumberStep)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.gasSafety.certificate",
                    "forms.checkComplianceAnswers.certificate.expired",
                    gasSafetyStartingStep,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.issueDate",
                    expiredGasSafetyIssueDate.toKotlinLocalDate(),
                    Destination.VisitableStep(mockState.gasSafetyIssueDateStep, childJourneyId),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.validUntil",
                    expiredGasSafetyIssueDate.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong()).toKotlinLocalDate(),
                    Destination.Nowhere(),
                ),
            )

        // Act
        val summaryRows =
            GasSafetyCyaSummaryRowsFactory(
                propertyHasCertificate,
                gasSafetyStartingStep,
                changeExemptionStep,
                mockUploadService,
                mockState,
                childJourneyId,
            ).createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }

    @Test
    fun `createRows returns correct rows when certificate is missing`() {
        // Arrange
        val propertyHasCertificate = false

        whenever(mockState.gasSafetyExemptionReasonStep).thenReturn(mockGasSafetyExemptionReasonStep)
        whenever(mockGasSafetyExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockGasSafetyExemptionMissingStep.outcome).thenReturn(Complete.COMPLETE)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.gasSafety.certificate",
                    "forms.checkComplianceAnswers.certificate.notAdded",
                    gasSafetyStartingStep,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.exemption",
                    "commonText.none",
                    changeExemptionStep,
                ),
            )

        // Act
        val summaryRows =
            GasSafetyCyaSummaryRowsFactory(
                propertyHasCertificate,
                gasSafetyStartingStep,
                changeExemptionStep,
                mockUploadService,
                mockState,
                childJourneyId,
            ).createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }

    @Test
    fun `createRows returns correct rows when exemption has been provided`() {
        // Arrange
        val propertyHasCertificate = false
        val exemptionReason = GasSafetyExemptionReason.NO_GAS_SUPPLY

        whenever(mockState.gasSafetyExemptionReasonStep).thenReturn(mockGasSafetyExemptionReasonStep)
        val exemptionReasonFormModel = GasSafetyExemptionReasonFormModel().apply { this.exemptionReason = exemptionReason }
        whenever(mockGasSafetyExemptionReasonStep.formModelIfReachableOrNull).thenReturn(exemptionReasonFormModel)
        whenever(mockGasSafetyExemptionConfirmationStep.outcome).thenReturn(Complete.COMPLETE)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.gasSafety.certificate",
                    "forms.checkComplianceAnswers.certificate.notRequired",
                    gasSafetyStartingStep,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.exemption",
                    exemptionReason,
                    changeExemptionStep,
                ),
            )

        // Act
        val summaryRows =
            GasSafetyCyaSummaryRowsFactory(
                propertyHasCertificate,
                gasSafetyStartingStep,
                changeExemptionStep,
                mockUploadService,
                mockState,
                childJourneyId,
            ).createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }

    @Test
    fun `createRows returns correct rows when OTHER exemption has been provided`() {
        // Arrange
        val propertyHasCertificate = false
        val otherReason = "Custom gas safety exemption reason"

        whenever(mockState.gasSafetyExemptionReasonStep).thenReturn(mockGasSafetyExemptionReasonStep)
        val exemptionReasonFormModel =
            GasSafetyExemptionReasonFormModel().apply { this.exemptionReason = GasSafetyExemptionReason.OTHER }
        whenever(mockGasSafetyExemptionReasonStep.formModelIfReachableOrNull).thenReturn(exemptionReasonFormModel)

        whenever(mockState.gasSafetyExemptionOtherReasonStep).thenReturn(mockGasSafetyExemptionOtherReasonStep)
        val otherReasonFormModel = GasSafetyExemptionOtherReasonFormModel().apply { this.otherReason = otherReason }
        whenever(mockGasSafetyExemptionOtherReasonStep.formModel).thenReturn(otherReasonFormModel)

        whenever(mockGasSafetyExemptionConfirmationStep.outcome).thenReturn(Complete.COMPLETE)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.gasSafety.certificate",
                    "forms.checkComplianceAnswers.certificate.notRequired",
                    gasSafetyStartingStep,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.exemption",
                    listOf(GasSafetyExemptionReason.OTHER, otherReason),
                    changeExemptionStep,
                ),
            )

        // Act
        val summaryRows =
            GasSafetyCyaSummaryRowsFactory(
                propertyHasCertificate,
                gasSafetyStartingStep,
                changeExemptionStep,
                mockUploadService,
                mockState,
                childJourneyId,
            ).createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }
}
