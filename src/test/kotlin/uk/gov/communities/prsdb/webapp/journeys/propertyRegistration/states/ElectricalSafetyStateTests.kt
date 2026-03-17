package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveElectricalCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyDateFormModel
import java.time.LocalDate

class ElectricalSafetyStateTests {
    @Test
    fun `getElectricalCertificateExpiryDateIfReachable returns the expiry date from state as a LocalDate`() {
        // Arrange
        val expiryDate = LocalDate.of(2030, 1, 1)
        val expiryDateFormModel = AnyDateFormModel().applyFromDate(expiryDate)
        val state = buildTestElectricalSafetyState(expiryDateFormModel = expiryDateFormModel)

        // Act
        val retrievedExpiryDate = state.getElectricalCertificateExpiryDateIfReachable()

        // Assert
        assertEquals(expiryDate.toKotlinLocalDate(), retrievedExpiryDate)
    }

    @Test
    fun `getElectricalCertificateExpiryDateIfReachable returns null if the expiry date is not set`() {
        val state = buildTestElectricalSafetyState(expiryDateStepShouldBeReachable = true)
        assertNull(state.getElectricalCertificateExpiryDateIfReachable())
    }

    @Test
    fun `getElectricalCertificateExpiryDateIfReachable returns null if formModelIfReachableOrNull is null`() {
        val state = buildTestElectricalSafetyState(expiryDateStepShouldBeReachable = false)
        assertNull(state.getElectricalCertificateExpiryDateIfReachable())
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns true if the certificate expiry date is in the past`() {
        // Arrange
        val expiryDate = LocalDate.now().minusDays(5)
        val expiryDateFormModel = AnyDateFormModel().applyFromDate(expiryDate)
        val state = buildTestElectricalSafetyState(expiryDateFormModel = expiryDateFormModel)

        // Act, Assert
        assertTrue(state.getElectricalCertificateIsOutdated()!!)
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns false if the certificate expiry date is in the future`() {
        // Arrange
        val expiryDate = LocalDate.now().plusDays(5)
        val expiryDateFormModel = AnyDateFormModel().applyFromDate(expiryDate)
        val state = buildTestElectricalSafetyState(expiryDateFormModel = expiryDateFormModel)

        // Act, Assert
        assertFalse(state.getElectricalCertificateIsOutdated()!!)
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns false if the certificate expires today`() {
        // Arrange
        val expiryDate = LocalDate.now()
        val expiryDateFormModel = AnyDateFormModel().applyFromDate(expiryDate)
        val state = buildTestElectricalSafetyState(expiryDateFormModel = expiryDateFormModel)

        // Act, Assert
        assertFalse(state.getElectricalCertificateIsOutdated()!!)
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns null if the expiryDate is null`() {
        val state = buildTestElectricalSafetyState()
        assertNull(state.getElectricalCertificateIsOutdated())
    }

    private fun buildTestElectricalSafetyState(
        expiryDateFormModel: AnyDateFormModel = AnyDateFormModel(),
        expiryDateStepShouldBeReachable: Boolean = true,
    ): ElectricalSafetyState =
        object : AbstractJourneyState(journeyStateService = mock()), ElectricalSafetyState {
            override val hasElectricalCertStep = mock<HasElectricalCertStep>()
            override val uploadElectricalCertStep = mock<UploadElectricalCertStep>()
            override val checkElectricalCertUploadsStep = mock<CheckElectricalCertUploadsStep>()
            override val removeElectricalCertUploadStep = mock<RemoveElectricalCertUploadStep>()
            override val electricalCertExpiredStep = mock<ElectricalCertExpiredStep>()
            override val electricalCertMissingStep = mock<ElectricalCertMissingStep>()
            override val provideElectricalCertLaterStep = mock<ProvideElectricalCertLaterStep>()
            override val checkElectricalSafetyAnswersStep = mock<CheckElectricalSafetyAnswersStep>()

            override val electricalCertExpiryDateStep =
                mock<ElectricalCertExpiryDateStep>().apply {
                    if (expiryDateStepShouldBeReachable) {
                        whenever(this.formModelIfReachableOrNull).thenReturn(expiryDateFormModel)
                    } else {
                        whenever(this.formModelIfReachableOrNull).thenReturn(null)
                    }
                }
        }

    private fun AnyDateFormModel.applyFromDate(date: LocalDate): AnyDateFormModel =
        apply {
            day = date.dayOfMonth.toString()
            month = date.monthValue.toString()
            year = date.year.toString()
        }
}
