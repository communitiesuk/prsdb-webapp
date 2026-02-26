package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
import java.time.LocalDate
import kotlin.test.assertEquals

class EicrCyaSummaryRowsFactoryTests {
    private val mockUploadService: UploadService = mock()

    private val mockEicrStep: EicrStep = mock()
    private val mockEicrIssueDateStep: EicrIssueDateStep = mock()
    private val mockEicrUploadConfirmationStep: EicrUploadConfirmationStep = mock()
    private val mockEicrOutdatedStep: EicrOutdatedStep = mock()
    private val mockEicrExemptionStep: EicrExemptionStep = mock()
    private val mockEicrExemptionReasonStep: EicrExemptionReasonStep = mock()
    private val mockEicrExemptionOtherReasonStep: EicrExemptionOtherReasonStep = mock()
    private val mockEicrExemptionConfirmationStep: EicrExemptionConfirmationStep = mock()
    private val mockEicrExemptionMissingStep: EicrExemptionMissingStep = mock()
    private val mockState: EicrState = mock()

    private val childJourneyId = "child-journey-123"
    private val eicrStartingStep = Destination.VisitableStep(mockEicrStep, childJourneyId)
    private val changeExemptionStep = Destination.VisitableStep(mockEicrExemptionStep, childJourneyId)

    private val eicrFileUploadId = 123L
    private val eicrFileUpload = MockPropertyComplianceData.createFileUpload(eicrFileUploadId)
    private val validEicrIssueDate = LocalDate.now().minusDays(5)
    private val expiredEicrIssueDate = LocalDate.now().minusYears((EICR_VALIDITY_YEARS + 1).toLong())

    @BeforeEach
    fun setupMocks() {
        whenever(mockState.eicrUploadConfirmationStep).thenReturn(mockEicrUploadConfirmationStep)
        whenever(mockState.eicrExemptionConfirmationStep).thenReturn(mockEicrExemptionConfirmationStep)
        whenever(mockState.eicrExemptionMissingStep).thenReturn(mockEicrExemptionMissingStep)
        whenever(mockState.eicrOutdatedStep).thenReturn(mockEicrOutdatedStep)
    }

    @Test
    fun `createRows returns correct summary rows when in-date certificate has been provided`() {
        // Arrange
        val propertyHasEicr = true
        val downloadUrl = "https://example.com/eicr.pdf"

        whenever(mockEicrUploadConfirmationStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEicrExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEicrExemptionMissingStep.outcome).thenReturn(null)
        whenever(mockEicrOutdatedStep.outcome).thenReturn(null)

        whenever(mockState.getEicrCertificateFileUploadId()).thenReturn(eicrFileUploadId)
        whenever(mockUploadService.getFileUploadById(eicrFileUploadId)).thenReturn(eicrFileUpload)
        whenever(mockUploadService.getDownloadUrl(eicrFileUpload, "eicr.pdf")).thenReturn(downloadUrl)

        whenever(mockState.eicrIssueDateStep).thenReturn(mockEicrIssueDateStep)
        whenever(mockState.getEicrCertificateIssueDate()).thenReturn(validEicrIssueDate.toKotlinLocalDate())

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.eicr.certificate",
                    "forms.checkComplianceAnswers.eicr.download",
                    eicrStartingStep,
                    downloadUrl,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.issueDate",
                    validEicrIssueDate.toKotlinLocalDate(),
                    Destination.VisitableStep(mockState.eicrIssueDateStep, childJourneyId),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.validUntil",
                    validEicrIssueDate.toKotlinLocalDate().plus(DatePeriod(years = EICR_VALIDITY_YEARS)),
                    null,
                ),
            )

        // Act
        val summaryRows =
            EicrCyaSummaryRowsFactory(
                propertyHasEicr,
                eicrStartingStep,
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
        val propertyHasEicr = true

        whenever(mockEicrUploadConfirmationStep.outcome).thenReturn(null)
        whenever(mockEicrExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEicrExemptionMissingStep.outcome).thenReturn(null)
        whenever(mockEicrOutdatedStep.outcome).thenReturn(Complete.COMPLETE)

        whenever(mockState.eicrIssueDateStep).thenReturn(mockEicrIssueDateStep)
        whenever(mockState.getEicrCertificateIssueDate()).thenReturn(expiredEicrIssueDate.toKotlinLocalDate())

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.eicr.certificate",
                    "forms.checkComplianceAnswers.certificate.expired",
                    eicrStartingStep,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.issueDate",
                    expiredEicrIssueDate.toKotlinLocalDate(),
                    Destination.VisitableStep(mockState.eicrIssueDateStep, childJourneyId),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.validUntil",
                    expiredEicrIssueDate.toKotlinLocalDate().plus(DatePeriod(years = EICR_VALIDITY_YEARS)),
                    null,
                ),
            )

        // Act
        val summaryRows =
            EicrCyaSummaryRowsFactory(
                propertyHasEicr,
                eicrStartingStep,
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
        val propertyHasEicr = false

        whenever(mockEicrUploadConfirmationStep.outcome).thenReturn(null)
        whenever(mockEicrExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEicrExemptionMissingStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEicrOutdatedStep.outcome).thenReturn(null)

        whenever(mockState.eicrExemptionReasonStep).thenReturn(mockEicrExemptionReasonStep)
        whenever(mockEicrExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.eicr.certificate",
                    "forms.checkComplianceAnswers.certificate.notAdded",
                    eicrStartingStep,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.exemption",
                    "commonText.none",
                    changeExemptionStep,
                ),
            )

        // Act
        val summaryRows =
            EicrCyaSummaryRowsFactory(
                propertyHasEicr,
                eicrStartingStep,
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
        val propertyHasEicr = false
        val exemptionReason = EicrExemptionReason.LONG_LEASE

        whenever(mockEicrUploadConfirmationStep.outcome).thenReturn(null)
        whenever(mockEicrExemptionConfirmationStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEicrExemptionMissingStep.outcome).thenReturn(null)
        whenever(mockEicrOutdatedStep.outcome).thenReturn(null)

        whenever(mockState.eicrExemptionReasonStep).thenReturn(mockEicrExemptionReasonStep)
        val exemptionReasonFormModel = EicrExemptionReasonFormModel().apply { this.exemptionReason = exemptionReason }
        whenever(mockEicrExemptionReasonStep.formModelIfReachableOrNull).thenReturn(exemptionReasonFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.eicr.certificate",
                    "forms.checkComplianceAnswers.certificate.notRequired",
                    eicrStartingStep,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.exemption",
                    exemptionReason,
                    changeExemptionStep,
                ),
            )

        // Act
        val summaryRows =
            EicrCyaSummaryRowsFactory(
                propertyHasEicr,
                eicrStartingStep,
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
        val propertyHasEicr = false
        val otherReason = "Custom EICR exemption reason"

        whenever(mockEicrUploadConfirmationStep.outcome).thenReturn(null)
        whenever(mockEicrExemptionConfirmationStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEicrExemptionMissingStep.outcome).thenReturn(null)
        whenever(mockEicrOutdatedStep.outcome).thenReturn(null)

        whenever(mockState.eicrExemptionReasonStep).thenReturn(mockEicrExemptionReasonStep)
        val exemptionReasonFormModel =
            EicrExemptionReasonFormModel().apply { this.exemptionReason = EicrExemptionReason.OTHER }
        whenever(mockEicrExemptionReasonStep.formModelIfReachableOrNull).thenReturn(exemptionReasonFormModel)

        whenever(mockState.eicrExemptionOtherReasonStep).thenReturn(mockEicrExemptionOtherReasonStep)
        val otherReasonFormModel = EicrExemptionOtherReasonFormModel().apply { this.otherReason = otherReason }
        whenever(mockEicrExemptionOtherReasonStep.formModel).thenReturn(otherReasonFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.eicr.certificate",
                    "forms.checkComplianceAnswers.certificate.notRequired",
                    eicrStartingStep,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.exemption",
                    listOf(EicrExemptionReason.OTHER, otherReason),
                    changeExemptionStep,
                ),
            )

        // Act
        val summaryRows =
            EicrCyaSummaryRowsFactory(
                propertyHasEicr,
                eicrStartingStep,
                changeExemptionStep,
                mockUploadService,
                mockState,
                childJourneyId,
            ).createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }
}
