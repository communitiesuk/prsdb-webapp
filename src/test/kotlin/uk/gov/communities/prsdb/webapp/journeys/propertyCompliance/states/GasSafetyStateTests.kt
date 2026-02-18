package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states

import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import java.time.LocalDate

class GasSafetyStateTests {
    @Test
    fun `getGasSafetyCertificateIssueDate returns the issue date from state as a LocalDate`() {
        // Arrange
        val issueDate = LocalDate.of(2020, 1, 1)
        val issueDateformModel = TodayOrPastDateFormModel.fromDateOrNull(issueDate)!!
        val state = buildTestGasSafetyState(issueDateFormModel = issueDateformModel)

        // Act
        val retrievedIssueDate = state.getGasSafetyCertificateIssueDate()

        // Assert
        assertEquals(issueDate.toKotlinLocalDate(), retrievedIssueDate)
    }

    @Test
    fun `getGasSafetyCertificateIssueDate returns null if the issue date is not set`() {
        val state = buildTestGasSafetyState()
        assertNull(state.getGasSafetyCertificateIssueDate())
    }

    @Test
    fun `getGasSafetyCertificateIsOutdated returns true if the certificate is older than GAS_SAFETY_CERT_VALIDITY_YEARS`() {
        // Arrange
        val issueDate = LocalDate.now().minusYears((GAS_SAFETY_CERT_VALIDITY_YEARS).toLong()).minusDays(5)
        val issueDateformModel = TodayOrPastDateFormModel.fromDateOrNull(issueDate)!!
        val state = buildTestGasSafetyState(issueDateFormModel = issueDateformModel)

        // Act, Assert
        assertTrue(state.getGasSafetyCertificateIsOutdated() == true)
    }

    @Test
    fun `getGasSafetyCertificateIsOutdated returns false if the certificate is newer than GAS_SAFETY_CERT_VALIDITY_YEARS`() {
        // Arrange
        val issueDate = LocalDate.now().minusYears((GAS_SAFETY_CERT_VALIDITY_YEARS).toLong()).plusDays(5)
        val issueDateformModel = TodayOrPastDateFormModel.fromDateOrNull(issueDate)!!
        val state = buildTestGasSafetyState(issueDateFormModel = issueDateformModel)

        // Act, Assert
        assertFalse(state.getGasSafetyCertificateIsOutdated() == true)
    }

    @Test
    fun `getGasSafetyCertificateIsOutdated returns null if the issueDate is null`() {
        val state = buildTestGasSafetyState()
        assertNull(state.getGasSafetyCertificateIsOutdated())
    }

    @Test
    fun `getGasSafetyCertificateFileUploadId returns the fileUploadId from state if found`() {
        // Arrange
        val fileUploadId = 123L
        val gasSafetyUploadFormModel = GasSafetyUploadCertificateFormModel()
        gasSafetyUploadFormModel.fileUploadId = fileUploadId
        val state = buildTestGasSafetyState(gasSafetyUploadFormModel = gasSafetyUploadFormModel)

        // Act
        val retrievedFileUploadId = state.getGasSafetyCertificateFileUploadId()

        // Assert
        assertEquals(fileUploadId, retrievedFileUploadId)
    }

    @Test
    fun `getGasSafetyCertificateFileUploadId returns null if the fileUploadId is not found in state`() {
        val state = buildTestGasSafetyState()
        assertNull(state.getGasSafetyCertificateFileUploadId())
    }

    private fun buildTestGasSafetyState(
        issueDateFormModel: TodayOrPastDateFormModel = TodayOrPastDateFormModel(),
        gasSafetyUploadFormModel: GasSafetyUploadCertificateFormModel = GasSafetyUploadCertificateFormModel(),
    ): GasSafetyState =
        object : AbstractJourneyState(journeyStateService = mock()), GasSafetyState {
            override val gasSafetyStep = mock<GasSafetyStep>()
            override val gasSafetyEngineerNumberStep = mock<GasSafetyEngineerNumberStep>()
            override val gasSafetyUploadConfirmationStep = mock<GasSafetyUploadConfirmationStep>()
            override val gasSafetyOutdatedStep = mock<GasSafetyOutdatedStep>()
            override val gasSafetyExemptionStep = mock<GasSafetyExemptionStep>()
            override val gasSafetyExemptionReasonStep = mock<GasSafetyExemptionReasonStep>()
            override val gasSafetyExemptionOtherReasonStep = mock<GasSafetyExemptionOtherReasonStep>()
            override val gasSafetyExemptionConfirmationStep = mock<GasSafetyExemptionConfirmationStep>()
            override val gasSafetyExemptionMissingStep = mock<GasSafetyExemptionMissingStep>()
            override val propertyId: Long = 123L

            override val gasSafetyIssueDateStep =
                mock<GasSafetyIssueDateStep>().apply {
                    whenever(this.formModelOrNull).thenReturn(issueDateFormModel)
                }

            override val gasSafetyCertificateUploadStep =
                mock<GasSafetyCertificateUploadStep>().apply {
                    whenever(this.formModelOrNull).thenReturn(gasSafetyUploadFormModel)
                }
            override val cyaStep: AbstractCheckYourAnswersStep<*> = mock()
            override var cyaChildJourneyIdIfInitialized: String? = "childJourneyId"
        }
}
