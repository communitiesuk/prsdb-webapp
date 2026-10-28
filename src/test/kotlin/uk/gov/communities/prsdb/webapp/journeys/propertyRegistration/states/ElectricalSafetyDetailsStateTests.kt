package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicrUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveEicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadEicrStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyDateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasElectricalCertFormModel
import java.time.LocalDate

class ElectricalSafetyDetailsStateTests {
    private val currentDate = DateTimeHelper().getCurrentDateInUK().toJavaLocalDate()

    @Test
    fun `getElectricalCertificateExpiryDateIfReachable returns the expiry date from state as a LocalDate`() {
        // Arrange
        val expiryDate = LocalDate.of(2030, 1, 1)
        val expiryDateFormModel = AnyDateFormModel().applyFromDate(expiryDate)
        val state = buildTestElectricalSafetyDetailState(expiryDateFormModel = expiryDateFormModel)

        // Act
        val retrievedExpiryDate = state.getElectricalCertificateExpiryDateIfReachable()

        // Assert
        assertEquals(expiryDate.toKotlinLocalDate(), retrievedExpiryDate)
    }

    @Test
    fun `getElectricalCertificateExpiryDateIfReachable returns null if the expiry date is not set`() {
        val state = buildTestElectricalSafetyDetailState(expiryDateStepShouldBeReachable = true)
        assertNull(state.getElectricalCertificateExpiryDateIfReachable())
    }

    @Test
    fun `getElectricalCertificateExpiryDateIfReachable returns null if formModelIfReachableOrNull is null`() {
        val state = buildTestElectricalSafetyDetailState(expiryDateStepShouldBeReachable = false)
        assertNull(state.getElectricalCertificateExpiryDateIfReachable())
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns true if the certificate expiry date is in the past`() {
        electricalCertTypes.forEach { certType ->
            val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate.minusDays(5), certType)
            assertTrue(state.getElectricalCertificateIsOutdated()!!, "for $certType")
        }
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns false if the certificate expiry date is in the future`() {
        electricalCertTypes.forEach { certType ->
            val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate.plusDays(5), certType)
            assertFalse(state.getElectricalCertificateIsOutdated()!!, "for $certType")
        }
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns true if the certificate expires today`() {
        electricalCertTypes.forEach { certType ->
            val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate, certType)
            assertTrue(state.getElectricalCertificateIsOutdated()!!, "for $certType")
        }
    }

    @Test
    fun `getElectricalCertificateIsOutdated throws if the expiry date is set but the cert type is missing`() {
        val state = buildTestElectricalSafetyDetailState(expiryDateFormModel = AnyDateFormModel().applyFromDate(currentDate))

        assertThrows<PrsdbWebException> { state.getElectricalCertificateIsOutdated() }
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns null if the expiryDate is null`() {
        val state = buildTestElectricalSafetyDetailState()
        assertNull(state.getElectricalCertificateIsOutdated())
    }

    @Test
    fun `getElectricalCertificateType returns HAS_EIC when EIC is selected`() {
        val formModel = HasElectricalCertFormModel().apply { electricalCertType = HasElectricalSafetyCertificate.HAS_EIC }
        val state = buildTestElectricalSafetyDetailState(hasElectricalCertFormModel = formModel)
        assertEquals(HasElectricalSafetyCertificate.HAS_EIC, state.getElectricalCertificateType())
    }

    @Test
    fun `getElectricalCertificateType returns HAS_EICR when EICR is selected`() {
        val formModel = HasElectricalCertFormModel().apply { electricalCertType = HasElectricalSafetyCertificate.HAS_EICR }
        val state = buildTestElectricalSafetyDetailState(hasElectricalCertFormModel = formModel)
        assertEquals(HasElectricalSafetyCertificate.HAS_EICR, state.getElectricalCertificateType())
    }

    @Test
    fun `getElectricalCertificateType returns null when step is not reachable`() {
        val state = buildTestElectricalSafetyDetailState(hasElectricalCertStepShouldBeReachable = false)
        assertNull(state.getElectricalCertificateType())
    }

    @Test
    fun `mapElectricalCertificateTypeToGlobalCertificateType returns Eic when HAS_EIC is selected`() {
        val formModel = HasElectricalCertFormModel().apply { electricalCertType = HasElectricalSafetyCertificate.HAS_EIC }
        val state = buildTestElectricalSafetyDetailState(hasElectricalCertFormModel = formModel)
        assertEquals(CertificateType.Eic, state.mapElectricalCertificateTypeToGlobalCertificateType())
    }

    @Test
    fun `mapElectricalCertificateTypeToGlobalCertificateType returns Eicr when HAS_EICR is selected`() {
        val formModel = HasElectricalCertFormModel().apply { electricalCertType = HasElectricalSafetyCertificate.HAS_EICR }
        val state = buildTestElectricalSafetyDetailState(hasElectricalCertFormModel = formModel)
        assertEquals(CertificateType.Eicr, state.mapElectricalCertificateTypeToGlobalCertificateType())
    }

    @Test
    fun `mapElectricalCertificateTypeToGlobalCertificateType returns null when NO_CERTIFICATE is selected`() {
        val state = buildTestElectricalSafetyDetailState(hasElectricalCertStepShouldBeReachable = false)
        assertNull(state.mapElectricalCertificateTypeToGlobalCertificateType())
    }

    @Test
    fun `mapElectricalCertificateTypeToGlobalCertificateType returns null when step is not reachable`() {
        val formModel = HasElectricalCertFormModel().apply { electricalCertType = HasElectricalSafetyCertificate.NO_CERTIFICATE }
        val state = buildTestElectricalSafetyDetailState(hasElectricalCertFormModel = formModel)
        assertNull(state.mapElectricalCertificateTypeToGlobalCertificateType())
    }

    private val electricalCertTypes = listOf(HasElectricalSafetyCertificate.HAS_EIC, HasElectricalSafetyCertificate.HAS_EICR)

    private fun buildTestElectricalSafetyDetailStateWithExpiryDate(
        expiryDate: LocalDate,
        certType: HasElectricalSafetyCertificate,
    ) = buildTestElectricalSafetyDetailState(
        expiryDateFormModel = AnyDateFormModel().applyFromDate(expiryDate),
        hasElectricalCertFormModel = HasElectricalCertFormModel().apply { electricalCertType = certType },
    )

    private fun buildTestElectricalSafetyDetailState(
        expiryDateFormModel: AnyDateFormModel = AnyDateFormModel(),
        expiryDateStepShouldBeReachable: Boolean = true,
        hasElectricalCertFormModel: HasElectricalCertFormModel? = null,
        hasElectricalCertStepShouldBeReachable: Boolean = true,
    ): ElectricalSafetyDetailState =
        object : AbstractJourneyState(journeyStateService = mock()), ElectricalSafetyDetailState {
            override val isOccupied: Boolean = true
            override val propertyOwnershipId: Long? = null
            override var eicrUploadMap: Map<Int, CertificateUpload> = mapOf()
            override var highestAssignedEicrMemberId: Int? = null
            override val uploadEicrStep = mock<UploadEicrStep>()
            override val hasUploadedEicr = mock<HasAnyInCollectionStep>()
            override val checkEicrUploadsStep = mock<CheckEicrUploadsStep>()
            override val removeEicrUploadStep = mock<RemoveEicrUploadStep>()
            override val eicrExpiredStep = mock<EicrExpiredStep>()
            override val electricalCertMissingStep = mock<ElectricalCertMissingStep>()
            override val allowProvideCertificateLaterRoute: Boolean = true
            override val provideElectricalCertLaterStep = mock<ProvideElectricalCertLaterStep>()

            override val hasElectricalCertStep =
                mock<HasElectricalCertStep>().apply {
                    if (hasElectricalCertStepShouldBeReachable) {
                        whenever(this.formModelIfReachableOrNull).thenReturn(hasElectricalCertFormModel)
                    } else {
                        whenever(this.formModelIfReachableOrNull).thenReturn(null)
                    }
                }

            override val eicrExpiryDateStep =
                mock<EicrExpiryDateStep>().apply {
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
