package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states

import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import java.time.LocalDate

class EicrStateTests {
    @Test
    fun `getEicrCertificateIssueDate returns the issue date from state as a LocalDate`() {
        // Arrange
        val issueDate = LocalDate.of(2020, 1, 1)
        val issueDateformModel = TodayOrPastDateFormModel.fromDateOrNull(issueDate)!!
        val state = buildTestEicrState(issueDateFormModel = issueDateformModel)

        // Act
        val retrievedIssueDate = state.getEicrCertificateIssueDate()

        // Assert
        assertEquals(issueDate.toKotlinLocalDate(), retrievedIssueDate)
    }

    @Test
    fun `getEicrCertificateIssueDate returns null if the issue date is not set`() {
        val state = buildTestEicrState()
        assertNull(state.getEicrCertificateIssueDate())
    }

    @Test
    fun `getEicrCertificateIsOutdated returns true if the certificate is older than EICR_VALIDITY_YEARS`() {
        // Arrange
        val issueDate = LocalDate.now().minusYears((EICR_VALIDITY_YEARS).toLong()).minusDays(5)
        val issueDateformModel = TodayOrPastDateFormModel.fromDateOrNull(issueDate)!!
        val state = buildTestEicrState(issueDateFormModel = issueDateformModel)

        // Act, Assert
        assertTrue(state.getEicrCertificateIsOutdated() == true)
    }

    @Test
    fun `getEicrCertificateIsOutdated returns false if the certificate is newer than EICR_VALIDITY_YEARS`() {
        // Arrange
        val issueDate = LocalDate.now().minusYears((EICR_VALIDITY_YEARS).toLong()).plusDays(5)
        val issueDateformModel = TodayOrPastDateFormModel.fromDateOrNull(issueDate)!!
        val state = buildTestEicrState(issueDateFormModel = issueDateformModel)

        // Act, Assert
        assertFalse(state.getEicrCertificateIsOutdated() == true)
    }

    @Test
    fun `getEicrCertificateIsOutdated returns null if the issueDate is null`() {
        val state = buildTestEicrState()
        assertNull(state.getEicrCertificateIsOutdated())
    }

    @Test
    fun `getEicrCertificateFileUploadId returns the fileUploadId from state if found`() {
        // Arrange
        val fileUploadId = 123L
        val eicrUploadFormModel = EicrUploadCertificateFormModel()
        eicrUploadFormModel.fileUploadId = fileUploadId
        val state = buildTestEicrState(eicrUploadFormModel = eicrUploadFormModel)

        // Act
        val retrievedFileUploadId = state.getEicrCertificateFileUploadId()

        // Assert
        assertEquals(fileUploadId, retrievedFileUploadId)
    }

    @Test
    fun `getEicrCertificateFileUploadId returns null if the fileUploadId is not found in state`() {
        val state = buildTestEicrState()
        assertNull(state.getEicrCertificateFileUploadId())
    }

    private fun buildTestEicrState(
        issueDateFormModel: TodayOrPastDateFormModel = TodayOrPastDateFormModel(),
        eicrUploadFormModel: EicrUploadCertificateFormModel = EicrUploadCertificateFormModel(),
    ): EicrState =
        object : AbstractJourneyState(journeyStateService = mock()), EicrState {
            override val eicrStep = mock<EicrStep>()
            override val eicrUploadConfirmationStep = mock<EicrUploadConfirmationStep>()
            override val eicrOutdatedStep = mock<EicrOutdatedStep>()
            override val eicrExemptionStep = mock<EicrExemptionStep>()
            override val eicrExemptionReasonStep = mock<EicrExemptionReasonStep>()
            override val eicrExemptionOtherReasonStep = mock<EicrExemptionOtherReasonStep>()
            override val eicrExemptionConfirmationStep = mock<EicrExemptionConfirmationStep>()
            override val eicrExemptionMissingStep = mock<EicrExemptionMissingStep>()
            override val propertyId: Long = 123L

            override val eicrIssueDateStep =
                mock<EicrIssueDateStep>().apply {
                    whenever(this.formModelIfReachableOrNull).thenReturn(issueDateFormModel)
                }

            override val eicrUploadStep =
                mock<EicrUploadStep>().apply {
                    whenever(this.formModelIfReachableOrNull).thenReturn(eicrUploadFormModel)
                }
            override val cyaStep: AbstractCheckYourAnswersStep<*> = mock()
            override var cyaChildJourneyIdIfInitialized: String? = "childJourneyId"
        }
}
