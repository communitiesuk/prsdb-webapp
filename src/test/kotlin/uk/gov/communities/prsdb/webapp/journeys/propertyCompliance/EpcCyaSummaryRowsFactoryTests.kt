package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import kotlin.test.assertEquals

class EpcCyaSummaryRowsFactoryTests {
    private val mockEpcCertificateUrlProvider: EpcCertificateUrlProvider = mock()

    private val mockEpcQuestionStep: EpcQuestionStep = mock()
    private val mockEpcExpiryCheckStep: EpcExpiryCheckStep = mock()
    private val mockEpcExemptionReasonStep: EpcExemptionReasonStep = mock()
    private val mockEpcExemptionConfirmationStep: EpcExemptionConfirmationStep = mock()
    private val mockEpcExpiredStep: EpcExpiredStep = mock()
    private val mockEpcMissingStep: EpcMissingStep = mock()
    private val mockEpcNotFoundStep: EpcNotFoundStep = mock()
    private val mockMeesExemptionCheckStep: MeesExemptionCheckStep = mock()
    private val mockMeesExemptionReasonStep: MeesExemptionReasonStep = mock()
    private val mockLowEnergyRatingStep: LowEnergyRatingStep = mock()
    private val mockState: EpcState = mock()

    private val childJourneyId = "child-journey-123"
    private val epcStartingStep = Destination.VisitableStep(mockEpcQuestionStep, childJourneyId)

    private val epcUrl = "https://example.com/epc"
    private val validEpcEnergyRating = "C"
    private val lowEpcEnergyRating = "F"
    private val expiredEpcExpiryDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(5, DAY)

    @BeforeEach
    fun setupMocks() {
        whenever(mockState.epcExemptionConfirmationStep).thenReturn(mockEpcExemptionConfirmationStep)
        whenever(mockState.epcExpiredStep).thenReturn(mockEpcExpiredStep)
        whenever(mockState.epcMissingStep).thenReturn(mockEpcMissingStep)
        whenever(mockState.epcNotFoundStep).thenReturn(mockEpcNotFoundStep)
        whenever(mockState.epcExpiryCheckStep).thenReturn(mockEpcExpiryCheckStep)
        whenever(mockState.meesExemptionCheckStep).thenReturn(mockMeesExemptionCheckStep)
        whenever(mockState.meesExemptionReasonStep).thenReturn(mockMeesExemptionReasonStep)
        whenever(mockState.epcExemptionReasonStep).thenReturn(mockEpcExemptionReasonStep)
    }

    @Test
    fun `createRows returns correct rows when in-date EPC has been provided`() {
        // Arrange
        val epcDetails = MockEpcData.createEpcDataModel(energyRating = validEpcEnergyRating)

        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockState.getNotNullAcceptedEpc()).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(null)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)

        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(null)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.epc.view",
                    epcStartingStep,
                    epcUrl,
                    valueUrlOpensNewTab = true,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryDate",
                    epcDetails.expiryDate,
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.energyRating",
                    validEpcEnergyRating.uppercase(),
                    null,
                ),
            )

        // Act
        val summaryRows =
            EpcCyaSummaryRowsFactory(epcStartingStep, mockEpcCertificateUrlProvider, mockState, childJourneyId)
                .createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `createRows returns correct rows when expired EPC has been provided`(tenancyStartedBeforeExpiry: Boolean) {
        // Arrange
        val epcDetails = MockEpcData.createEpcDataModel(expiryDate = expiredEpcExpiryDate)

        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockState.getNotNullAcceptedEpc()).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)

        val expiryCheckFormModel = EpcExpiryCheckFormModel().apply { this.tenancyStartedBeforeExpiry = tenancyStartedBeforeExpiry }
        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(expiryCheckFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.epc.viewExpired",
                    epcStartingStep,
                    epcUrl,
                    valueUrlOpensNewTab = true,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryDate",
                    epcDetails.expiryDate,
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryCheck",
                    tenancyStartedBeforeExpiry,
                    Destination.VisitableStep(mockState.epcExpiryCheckStep, childJourneyId),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.energyRating",
                    validEpcEnergyRating.uppercase(),
                    null,
                ),
            )

        // Act
        val summaryRows =
            EpcCyaSummaryRowsFactory(epcStartingStep, mockEpcCertificateUrlProvider, mockState, childJourneyId)
                .createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }

    @Test
    fun `createRows returns correct rows when EPC is missing`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(null)

        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(null)
        whenever(mockEpcMissingStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.certificate.notAdded",
                    epcStartingStep,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.exemption",
                    "commonText.none",
                    epcStartingStep,
                ),
            )

        // Act
        val summaryRows =
            EpcCyaSummaryRowsFactory(epcStartingStep, mockEpcCertificateUrlProvider, mockState, childJourneyId)
                .createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }

    @Test
    fun `createRows returns correct rows when EPC was not found`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(null)

        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(null)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(Complete.COMPLETE)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.certificate.notAdded",
                    epcStartingStep,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.exemption",
                    "commonText.none",
                    epcStartingStep,
                ),
            )

        // Act
        val summaryRows =
            EpcCyaSummaryRowsFactory(epcStartingStep, mockEpcCertificateUrlProvider, mockState, childJourneyId)
                .createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }

    @Test
    fun `createRows returns correct rows when EPC exemption has been provided`() {
        // Arrange
        val exemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION

        whenever(mockState.acceptedEpc).thenReturn(null)

        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEpcExpiredStep.outcome).thenReturn(null)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)

        val exemptionReasonFormModel = EpcExemptionReasonFormModel().apply { this.exemptionReason = exemptionReason }
        whenever(mockEpcExemptionReasonStep.formModelIfReachableOrNull).thenReturn(exemptionReasonFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.certificate.notRequired",
                    epcStartingStep,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.exemption",
                    exemptionReason,
                    Destination.VisitableStep(mockState.epcExemptionReasonStep, childJourneyId),
                ),
            )

        // Act
        val summaryRows =
            EpcCyaSummaryRowsFactory(epcStartingStep, mockEpcCertificateUrlProvider, mockState, childJourneyId)
                .createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }

    @Test
    fun `createRows returns correct rows when in-date EPC has low rating with MEES exemption`() {
        // Arrange
        val epcDetails = MockEpcData.createEpcDataModel(energyRating = lowEpcEnergyRating)
        val meesExemption = MeesExemptionReason.HIGH_COST

        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockState.getNotNullAcceptedEpc()).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(null)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)

        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(null)

        whenever(mockMeesExemptionCheckStep.isStepReachable).thenReturn(true)
        val meesExemptionFormModel = MeesExemptionReasonFormModel().apply { this.exemptionReason = meesExemption }
        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(meesExemptionFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.epc.view",
                    epcStartingStep,
                    epcUrl,
                    valueUrlOpensNewTab = true,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryDate",
                    epcDetails.expiryDate,
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.energyRating",
                    lowEpcEnergyRating.uppercase(),
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.meesExemption",
                    meesExemption,
                    Destination.VisitableStep(mockState.meesExemptionReasonStep, childJourneyId),
                ),
            )

        // Act
        val summaryRows =
            EpcCyaSummaryRowsFactory(epcStartingStep, mockEpcCertificateUrlProvider, mockState, childJourneyId)
                .createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }

    @Test
    fun `createRows returns correct rows when in-date EPC has low rating with no MEES exemption`() {
        // Arrange
        val epcDetails = MockEpcData.createEpcDataModel(energyRating = lowEpcEnergyRating)

        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockState.getNotNullAcceptedEpc()).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(null)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)

        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(null)

        whenever(mockMeesExemptionCheckStep.isStepReachable).thenReturn(true)
        whenever(mockMeesExemptionCheckStep.routeSegment).thenReturn("/mees-exemption-check")
        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.epc.view",
                    epcStartingStep,
                    epcUrl,
                    valueUrlOpensNewTab = true,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryDate",
                    epcDetails.expiryDate,
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.energyRating",
                    lowEpcEnergyRating.uppercase(),
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.meesExemption",
                    "commonText.none",
                    Destination.VisitableStep(mockState.meesExemptionCheckStep, childJourneyId),
                ),
            )

        // Act
        val summaryRows =
            EpcCyaSummaryRowsFactory(epcStartingStep, mockEpcCertificateUrlProvider, mockState, childJourneyId)
                .createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }

    @Test
    fun `createRows returns correct rows when expired EPC has low rating with MEES exemption`() {
        // Arrange
        val epcDetails = MockEpcData.createEpcDataModel(energyRating = lowEpcEnergyRating, expiryDate = expiredEpcExpiryDate)
        val meesExemption = MeesExemptionReason.PROPERTY_DEVALUATION

        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockState.getNotNullAcceptedEpc()).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)

        val expiryCheckFormModel = EpcExpiryCheckFormModel().apply { this.tenancyStartedBeforeExpiry = tenancyStartedBeforeExpiry }
        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(expiryCheckFormModel)

        whenever(mockMeesExemptionCheckStep.isStepReachable).thenReturn(true)
        val meesExemptionFormModel = MeesExemptionReasonFormModel().apply { this.exemptionReason = meesExemption }
        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(meesExemptionFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.epc.viewExpired",
                    epcStartingStep,
                    epcUrl,
                    valueUrlOpensNewTab = true,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryDate",
                    epcDetails.expiryDate,
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryCheck",
                    false,
                    Destination.VisitableStep(mockState.epcExpiryCheckStep, childJourneyId),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.energyRating",
                    lowEpcEnergyRating.uppercase(),
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.meesExemption",
                    meesExemption,
                    epcStartingStep,
                ),
            )

        // Act
        val summaryRows =
            EpcCyaSummaryRowsFactory(epcStartingStep, mockEpcCertificateUrlProvider, mockState, childJourneyId)
                .createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }

    @Test
    fun `createRows returns correct rows when expired EPC has low rating with no MEES exemption`() {
        // Arrange
        val epcDetails = MockEpcData.createEpcDataModel(energyRating = lowEpcEnergyRating, expiryDate = expiredEpcExpiryDate)
        val tenancyStartedBeforeExpiry = false

        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockState.getNotNullAcceptedEpc()).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)

        val expiryCheckFormModel = EpcExpiryCheckFormModel().apply { this.tenancyStartedBeforeExpiry = tenancyStartedBeforeExpiry }
        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(expiryCheckFormModel)

        whenever(mockMeesExemptionCheckStep.isStepReachable).thenReturn(true)
        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.epc.viewExpired",
                    epcStartingStep,
                    epcUrl,
                    valueUrlOpensNewTab = true,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryDate",
                    epcDetails.expiryDate,
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryCheck",
                    tenancyStartedBeforeExpiry,
                    Destination.VisitableStep(mockState.epcExpiryCheckStep, childJourneyId),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.energyRating",
                    lowEpcEnergyRating.uppercase(),
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.meesExemption",
                    "commonText.none",
                    epcStartingStep,
                ),
            )

        // Act
        val summaryRows =
            EpcCyaSummaryRowsFactory(epcStartingStep, mockEpcCertificateUrlProvider, mockState, childJourneyId)
                .createRows()

        // Assert
        assertEquals(expectedRows, summaryRows)
    }
}
