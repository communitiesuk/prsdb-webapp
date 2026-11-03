package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicrUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveEicUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveEicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadEicStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadEicrStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyDateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasElectricalCertFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import java.time.LocalDate

class ElectricalSafetyDetailsStateTests {
    private val currentDate = DateTimeHelper().getCurrentDateInUK().toJavaLocalDate()

    @Test
    fun `getElectricalCertificateExpiryDateIfReachable returns the EIC expiry date when the EIC branch is reachable`() {
        val expiryDate = LocalDate.of(2030, 1, 1)
        val state =
            buildTestElectricalSafetyDetailState(
                electricalCertMode = HasElectricalCertMode.HAS_EIC,
                eicExpiryDateFormModel = AnyDateFormModel().applyFromDate(expiryDate),
            )

        assertEquals(expiryDate.toKotlinLocalDate(), state.getElectricalCertificateExpiryDateIfReachable())
    }

    @Test
    fun `getElectricalCertificateExpiryDateIfReachable returns the EICR expiry date when the EICR branch is reachable`() {
        val expiryDate = LocalDate.of(2030, 1, 1)
        val state =
            buildTestElectricalSafetyDetailState(
                electricalCertMode = HasElectricalCertMode.HAS_EICR,
                eicrExpiryDateFormModel = AnyDateFormModel().applyFromDate(expiryDate),
            )

        assertEquals(expiryDate.toKotlinLocalDate(), state.getElectricalCertificateExpiryDateIfReachable())
    }

    // TODO PDJB-1679: Remove this test when the EIC_ISSUE_DATE flag is removed
    @Test
    fun `getEicExpiryDateIfReachable returns the EIC expiry date when the EIC expiry date step is reachable`() {
        val expiryDate = LocalDate.of(2030, 1, 1)
        val state = buildTestElectricalSafetyDetailState(eicExpiryDateFormModel = AnyDateFormModel().applyFromDate(expiryDate))

        assertEquals(expiryDate.toKotlinLocalDate(), state.getEicExpiryDateIfReachable())
    }

    @Test
    fun `getEicrExpiryDateIfReachable returns the EICR expiry date when the EICR expiry date step is reachable`() {
        val expiryDate = LocalDate.of(2030, 1, 1)
        val state = buildTestElectricalSafetyDetailState(eicrExpiryDateFormModel = AnyDateFormModel().applyFromDate(expiryDate))

        assertEquals(expiryDate.toKotlinLocalDate(), state.getEicrExpiryDateIfReachable())
    }

    @Test
    fun `getElectricalCertificateExpiryDateIfReachable returns null if the expiry date is not set`() {
        val state =
            buildTestElectricalSafetyDetailState(
                electricalCertMode = HasElectricalCertMode.HAS_EICR,
                eicrExpiryDateFormModel = AnyDateFormModel(),
            )
        assertNull(state.getElectricalCertificateExpiryDateIfReachable())
    }

    @Test
    fun `getElectricalCertificateExpiryDateIfReachable returns null if neither expiry date step is reachable`() {
        val state = buildTestElectricalSafetyDetailState()
        assertNull(state.getElectricalCertificateExpiryDateIfReachable())
    }

    // TODO PDJB-1679: Remove this test when the EIC_ISSUE_DATE flag is removed
    @Test
    fun `getElectricalCertificateIsOutdated returns true if the EIC expiry date is in the past`() {
        val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate.minusDays(5), HasElectricalSafetyCertificate.HAS_EIC)
        assertTrue(state.getElectricalCertificateIsOutdated()!!)
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns true if the EICR expiry date is in the past`() {
        val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate.minusDays(5), HasElectricalSafetyCertificate.HAS_EICR)
        assertTrue(state.getElectricalCertificateIsOutdated()!!)
    }

    // TODO PDJB-1679: Remove this test when the EIC_ISSUE_DATE flag is removed
    @Test
    fun `getElectricalCertificateIsOutdated returns false if the EIC expiry date is in the future`() {
        val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate.plusDays(5), HasElectricalSafetyCertificate.HAS_EIC)
        assertFalse(state.getElectricalCertificateIsOutdated()!!)
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns false if the EICR expiry date is in the future`() {
        val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate.plusDays(5), HasElectricalSafetyCertificate.HAS_EICR)
        assertFalse(state.getElectricalCertificateIsOutdated()!!)
    }

    // TODO PDJB-1679: Remove this test when the EIC_ISSUE_DATE flag is removed
    @Test
    fun `getElectricalCertificateIsOutdated returns false if the EIC expires today`() {
        val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate, HasElectricalSafetyCertificate.HAS_EIC)
        assertFalse(state.getElectricalCertificateIsOutdated()!!)
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns false if the EICR expires today`() {
        val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate, HasElectricalSafetyCertificate.HAS_EICR)
        assertFalse(state.getElectricalCertificateIsOutdated()!!)
    }

    // TODO PDJB-1679: Remove this test when the EIC_ISSUE_DATE flag is removed
    @Test
    fun `getEicIsOutdated returns true if the EIC expiry date is in the past`() {
        val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate.minusDays(5), HasElectricalSafetyCertificate.HAS_EIC)
        assertTrue(state.getEicIsOutdated()!!)
    }

    @Test
    fun `getEicIsOutdated returns null if only an EICR expiry date has been provided`() {
        val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate.minusDays(5), HasElectricalSafetyCertificate.HAS_EICR)
        assertNull(state.getEicIsOutdated())
    }

    @Test
    fun `getEicrIsOutdated returns true if the EICR expiry date is in the past`() {
        val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate.minusDays(5), HasElectricalSafetyCertificate.HAS_EICR)
        assertTrue(state.getEicrIsOutdated()!!)
    }

    // TODO PDJB-1679: Use an EIC issue date when the EIC_ISSUE_DATE flag is removed
    @Test
    fun `getEicrIsOutdated returns null if only an EIC expiry date has been provided`() {
        val state = buildTestElectricalSafetyDetailStateWithExpiryDate(currentDate.minusDays(5), HasElectricalSafetyCertificate.HAS_EIC)
        assertNull(state.getEicrIsOutdated())
    }

    @Test
    fun `getElectricalCertificateIsOutdated returns null if the expiryDate is null`() {
        val state = buildTestElectricalSafetyDetailState()
        assertNull(state.getElectricalCertificateIsOutdated())
    }

    @Test
    fun `getEicIssueDateIfReachable returns the EIC issue date when the EIC issue date step is reachable`() {
        val issueDate = LocalDate.of(2024, 3, 20)
        val state =
            buildTestElectricalSafetyDetailState(
                electricalCertMode = HasElectricalCertMode.HAS_EIC,
                eicIssueDateFormModel = TodayOrPastDateFormModel().applyFromDate(issueDate),
            )

        assertEquals(issueDate.toKotlinLocalDate(), state.getEicIssueDateIfReachable())
    }

    @Test
    fun `getEicIssueDateIfReachable returns null when the EIC issue date step is not reachable`() {
        val state = buildTestElectricalSafetyDetailState(electricalCertMode = HasElectricalCertMode.HAS_EIC)

        assertNull(state.getEicIssueDateIfReachable())
    }

    @Test
    fun `getEicIsOutdated returns false if the EIC was issued today`() {
        val state = buildTestElectricalSafetyDetailStateWithIssueDate(currentDate)
        assertFalse(state.getEicIsOutdated()!!)
    }

    @Test
    fun `getEicIsOutdated returns false if the EIC was issued exactly 5 years ago`() {
        val state = buildTestElectricalSafetyDetailStateWithIssueDate(currentDate.minusYears(5))
        assertFalse(state.getEicIsOutdated()!!)
    }

    @Test
    fun `getEicIsOutdated returns true if the EIC was issued 5 years and 1 day ago`() {
        val state = buildTestElectricalSafetyDetailStateWithIssueDate(currentDate.minusYears(5).minusDays(1))
        assertTrue(state.getEicIsOutdated()!!)
    }

    @Test
    fun `getElectricalCertificateIsOutdated uses the EIC issue date when the EIC issue date step is reachable`() {
        val state = buildTestElectricalSafetyDetailStateWithIssueDate(currentDate.minusYears(6))
        assertTrue(state.getElectricalCertificateIsOutdated()!!)
    }

    @Test
    fun `electricalUploadIds returns the EIC upload ids when the EIC upload step is reachable`() {
        val state =
            buildTestElectricalSafetyDetailState(
                electricalCertMode = HasElectricalCertMode.HAS_EIC,
                eicUploadStepShouldBeReachable = true,
            )
        state.eicUploadMap = mapOf(1 to CertificateUpload(11L, "eic.pdf"))
        state.eicrUploadMap = mapOf(1 to CertificateUpload(22L, "eicr.pdf"))

        assertEquals(listOf(11L), state.electricalUploadIds)
    }

    @Test
    fun `electricalUploadIds returns the EICR upload ids when the EICR upload step is reachable`() {
        val state =
            buildTestElectricalSafetyDetailState(
                electricalCertMode = HasElectricalCertMode.HAS_EICR,
                eicrUploadStepShouldBeReachable = true,
            )
        state.eicUploadMap = mapOf(1 to CertificateUpload(11L, "eic.pdf"))
        state.eicrUploadMap = mapOf(1 to CertificateUpload(22L, "eicr.pdf"))

        assertEquals(listOf(22L), state.electricalUploadIds)
    }

    @Test
    fun `electricalUploadIds returns an empty list when neither upload step is reachable`() {
        val state = buildTestElectricalSafetyDetailState()
        state.eicUploadMap = mapOf(1 to CertificateUpload(11L, "eic.pdf"))
        state.eicrUploadMap = mapOf(1 to CertificateUpload(22L, "eicr.pdf"))

        assertEquals(emptyList<Long>(), state.electricalUploadIds)
    }

    @Test
    fun `electricalUploadIds returns an empty list when the selected branch's upload step is not reachable`() {
        val state =
            buildTestElectricalSafetyDetailState(
                electricalCertMode = HasElectricalCertMode.HAS_EIC,
                eicrUploadStepShouldBeReachable = true,
            )
        state.eicUploadMap = mapOf(1 to CertificateUpload(11L, "eic.pdf"))
        state.eicrUploadMap = mapOf(1 to CertificateUpload(22L, "eicr.pdf"))

        assertEquals(emptyList<Long>(), state.electricalUploadIds)
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

    private fun buildTestElectricalSafetyDetailStateWithExpiryDate(
        expiryDate: LocalDate,
        certType: HasElectricalSafetyCertificate,
    ): ElectricalSafetyDetailState {
        val expiryDateFormModel = AnyDateFormModel().applyFromDate(expiryDate)
        return buildTestElectricalSafetyDetailState(
            // TODO PDJB-1679: Remove EIC support from this helper when the EIC_ISSUE_DATE flag is removed
            eicExpiryDateFormModel = if (certType == HasElectricalSafetyCertificate.HAS_EIC) expiryDateFormModel else null,
            eicrExpiryDateFormModel = if (certType == HasElectricalSafetyCertificate.HAS_EICR) expiryDateFormModel else null,
            hasElectricalCertFormModel = HasElectricalCertFormModel().apply { electricalCertType = certType },
            electricalCertMode =
                if (certType == HasElectricalSafetyCertificate.HAS_EIC) HasElectricalCertMode.HAS_EIC else HasElectricalCertMode.HAS_EICR,
        )
    }

    private fun buildTestElectricalSafetyDetailStateWithIssueDate(issueDate: LocalDate): ElectricalSafetyDetailState =
        buildTestElectricalSafetyDetailState(
            eicIssueDateFormModel = TodayOrPastDateFormModel().applyFromDate(issueDate),
            hasElectricalCertFormModel =
                HasElectricalCertFormModel().apply { electricalCertType = HasElectricalSafetyCertificate.HAS_EIC },
            electricalCertMode = HasElectricalCertMode.HAS_EIC,
        )

    // A null expiry date form model means that expiry date step is not reachable
    private fun buildTestElectricalSafetyDetailState(
        electricalCertMode: HasElectricalCertMode? = null,
        // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
        eicExpiryDateFormModel: AnyDateFormModel? = null,
        eicIssueDateFormModel: TodayOrPastDateFormModel? = null,
        eicrExpiryDateFormModel: AnyDateFormModel? = null,
        eicUploadStepShouldBeReachable: Boolean = false,
        eicrUploadStepShouldBeReachable: Boolean = false,
        hasElectricalCertFormModel: HasElectricalCertFormModel? = null,
        hasElectricalCertStepShouldBeReachable: Boolean = true,
    ): ElectricalSafetyDetailState =
        object : AbstractJourneyState(journeyStateService = mock()), ElectricalSafetyDetailState {
            override val isOccupied: Boolean = true
            override val propertyOwnershipId: Long? = null

            // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
            override val isEicIssueDateEnabled: Boolean = true
            override var eicrUploadMap: Map<Int, CertificateUpload> = mapOf()
            override var highestAssignedEicrMemberId: Int? = null
            override var eicUploadMap: Map<Int, CertificateUpload> = mapOf()
            override var highestAssignedEicMemberId: Int? = null
            override val hasUploadedEicr = mock<HasAnyInCollectionStep>()
            override val checkEicrUploadsStep = mock<CheckEicrUploadsStep>()
            override val removeEicrUploadStep = mock<RemoveEicrUploadStep>()
            override val eicrExpiredStep = mock<EicrExpiredStep>()
            override val hasUploadedEic = mock<HasAnyInCollectionStep>()
            override val checkEicUploadsStep = mock<CheckEicUploadsStep>()
            override val removeEicUploadStep = mock<RemoveEicUploadStep>()
            override val eicExpiredStep = mock<EicExpiredStep>()
            override val electricalCertMissingStep = mock<ElectricalCertMissingStep>()
            override val allowProvideCertificateLaterRoute: Boolean = true
            override val provideElectricalCertLaterStep = mock<ProvideElectricalCertLaterStep>()

            override val hasElectricalCertStep =
                mock<HasElectricalCertStep>().apply {
                    whenever(this.outcome).thenReturn(electricalCertMode)
                    if (hasElectricalCertStepShouldBeReachable) {
                        whenever(this.formModelIfReachableOrNull).thenReturn(hasElectricalCertFormModel)
                    } else {
                        whenever(this.formModelIfReachableOrNull).thenReturn(null)
                    }
                }

            // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
            override val eicExpiryDateStep =
                mock<EicExpiryDateStep>().apply {
                    whenever(this.formModelIfReachableOrNull).thenReturn(eicExpiryDateFormModel)
                }

            override val eicIssueDateStep =
                mock<EicIssueDateStep>().apply {
                    whenever(this.formModelIfReachableOrNull).thenReturn(eicIssueDateFormModel)
                }

            override val eicrExpiryDateStep =
                mock<EicrExpiryDateStep>().apply {
                    whenever(this.formModelIfReachableOrNull).thenReturn(eicrExpiryDateFormModel)
                }

            override val uploadEicStep =
                mock<UploadEicStep>().apply {
                    whenever(this.isStepReachable).thenReturn(eicUploadStepShouldBeReachable)
                }

            override val uploadEicrStep =
                mock<UploadEicrStep>().apply {
                    whenever(this.isStepReachable).thenReturn(eicrUploadStepShouldBeReachable)
                }
        }

    private fun AnyDateFormModel.applyFromDate(date: LocalDate): AnyDateFormModel =
        apply {
            day = date.dayOfMonth.toString()
            month = date.monthValue.toString()
            year = date.year.toString()
        }

    private fun TodayOrPastDateFormModel.applyFromDate(date: LocalDate): TodayOrPastDateFormModel =
        apply {
            day = date.dayOfMonth.toString()
            month = date.monthValue.toString()
            year = date.year.toString()
        }
}
