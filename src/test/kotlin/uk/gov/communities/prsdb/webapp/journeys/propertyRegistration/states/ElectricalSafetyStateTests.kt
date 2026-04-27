package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveElectricalCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyDateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasElectricalCertFormModel
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
    fun `getElectricalCertificateIsOutdated returns true if the certificate expires today`() {
        // Arrange
        val expiryDate = LocalDate.now()
        val expiryDateFormModel = AnyDateFormModel().applyFromDate(expiryDate)
        val state = buildTestElectricalSafetyState(expiryDateFormModel = expiryDateFormModel)

        // Act, Assert
        assertTrue(state.getElectricalCertificateIsOutdated()!!)
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns null if the expiryDate is null`() {
        val state = buildTestElectricalSafetyState()
        assertNull(state.getElectricalCertificateIsOutdated())
    }

    @Test
    fun `getElectricalCertificateType returns HAS_EIC when EIC is selected`() {
        val formModel = HasElectricalCertFormModel().apply { electricalCertType = HasElectricalSafetyCertificate.HAS_EIC }
        val state = buildTestElectricalSafetyState(hasElectricalCertFormModel = formModel)
        assertEquals(HasElectricalSafetyCertificate.HAS_EIC, state.getElectricalCertificateType())
    }

    @Test
    fun `getElectricalCertificateType returns HAS_EICR when EICR is selected`() {
        val formModel = HasElectricalCertFormModel().apply { electricalCertType = HasElectricalSafetyCertificate.HAS_EICR }
        val state = buildTestElectricalSafetyState(hasElectricalCertFormModel = formModel)
        assertEquals(HasElectricalSafetyCertificate.HAS_EICR, state.getElectricalCertificateType())
    }

    @Test
    fun `getElectricalCertificateType returns null when step is not reachable`() {
        val state = buildTestElectricalSafetyState(hasElectricalCertStepShouldBeReachable = false)
        assertNull(state.getElectricalCertificateType())
    }

    private fun buildTestElectricalSafetyState(
        expiryDateFormModel: AnyDateFormModel = AnyDateFormModel(),
        expiryDateStepShouldBeReachable: Boolean = true,
        hasElectricalCertFormModel: HasElectricalCertFormModel? = null,
        hasElectricalCertStepShouldBeReachable: Boolean = true,
    ): ElectricalSafetyState =
        object : AbstractJourneyState(journeyStateService = mock()), ElectricalSafetyState {
            override val isOccupied: Boolean = true
            override var electricalUploadMap: Map<Int, CertificateUpload> = mapOf()
            override var highestAssignedElectricalMemberId: Int? = null
            override val uploadElectricalCertStep = mock<UploadElectricalCertStep>()
            override val hasUploadedElectricalCert = mock<HasAnyInCollectionStep>()
            override val checkElectricalCertUploadsStep = mock<CheckElectricalCertUploadsStep>()
            override val removeElectricalCertUploadStep = mock<RemoveElectricalCertUploadStep>()
            override val electricalCertExpiredStep = mock<ElectricalCertExpiredStep>()
            override val electricalCertMissingStep = mock<ElectricalCertMissingStep>()
            override val provideElectricalCertLaterStep = mock<ProvideElectricalCertLaterStep>()
            override val checkElectricalSafetyAnswersStep = mock<CheckElectricalSafetyAnswersStep>()
            override val electricalSafetyDetailsTask =
                mock<ElectricalSafetyDetailsTask>()

            override val hasElectricalCertStep =
                mock<HasElectricalCertStep>().apply {
                    if (hasElectricalCertStepShouldBeReachable) {
                        whenever(this.formModelIfReachableOrNull).thenReturn(hasElectricalCertFormModel)
                    } else {
                        whenever(this.formModelIfReachableOrNull).thenReturn(null)
                    }
                }

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
