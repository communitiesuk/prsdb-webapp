package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.UploadedFileUrl
import uk.gov.communities.prsdb.webapp.services.UploadService

@ExtendWith(MockitoExtension::class)
class GasSafetyRegistrationCyaSummaryRowsFactoryTests {
    @Mock
    lateinit var mockState: GasSafetyState

    private val mockHasGasSupplyStep: HasGasSupplyStep = mock()
    private val mockHasGasCertStep: HasGasCertStep = mock()
    private val mockGasCertIssueDateStep: GasCertIssueDateStep = mock()
    private val mockCheckGasCertUploadsStep: CheckGasCertUploadsStep = mock()
    private val mockUploadService: UploadService = mock()

    private fun fileUploadWithStatus(status: FileUploadStatus): FileUpload =
        mock<FileUpload>().also { whenever(it.status).thenReturn(status) }

    private fun stubUpload(
        fileUploadId: Long,
        status: FileUploadStatus,
        downloadUrl: String? = null,
    ) {
        val fileUpload = fileUploadWithStatus(status)
        whenever(mockUploadService.getFileUploadById(fileUploadId)).thenReturn(fileUpload)
        whenever(mockUploadService.getDownloadUrlOrNull(eq(fileUpload), any())).thenReturn(downloadUrl)
    }

    private fun setupCommonStateMocks() {
        whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
        whenever(mockState.hasGasCertStep).thenReturn(mockHasGasCertStep)
        whenever(mockHasGasSupplyStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockHasGasCertStep.currentJourneyId).thenReturn("test-journey-id")
    }

    @Nested
    inner class NoGasSupply {
        @Test
        fun `createGasSupplyRows returns single row with false when no gas supply`() {
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockHasGasSupplyStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.NO)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(1, gasSupplyRows.size)
            assertEquals(false, gasSupplyRows[0].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertEquals("checkGasSafety.noGasSupplyInsetText", factory.getInsetTextKey())
        }
    }

    @Nested
    inner class UploadedCertificate {
        @Test
        fun `factory returns scanned uploads as UploadedFileUrl with download link`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)

            val issueDate = LocalDate(2024, 6, 15)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(issueDate)

            whenever(mockState.gasCertIssueDateStep).thenReturn(mockGasCertIssueDateStep)
            whenever(mockState.checkGasCertUploadsStep).thenReturn(mockCheckGasCertUploadsStep)
            whenever(mockGasCertIssueDateStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockCheckGasCertUploadsStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.gasUploadMap).thenReturn(
                mapOf(1 to CertificateUpload(1L, "cert.pdf")),
            )
            stubUpload(1L, FileUploadStatus.SCANNED, downloadUrl = "/download/cert.pdf")

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(1, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(3, certRows.size)
            assertEquals(true, certRows[0].fieldValue)
            assertEquals(issueDate, certRows[1].fieldValue)
            assertEquals(
                listOf(
                    UploadedFileUrl(
                        messageKey = "propertyDetails.complianceInformation.gasSafety.downloadCertificate",
                        displayName = "cert.pdf",
                        url = "/download/cert.pdf",
                    ),
                ),
                certRows[2].fieldValue,
            )

            assertNull(factory.getInsetTextKey())
        }

        @Test
        fun `factory returns quarantined uploads as UploadedFileUrl with pending scan messageKey and no url`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(LocalDate(2024, 6, 15))
            whenever(mockState.gasCertIssueDateStep).thenReturn(mockGasCertIssueDateStep)
            whenever(mockState.checkGasCertUploadsStep).thenReturn(mockCheckGasCertUploadsStep)
            whenever(mockGasCertIssueDateStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockCheckGasCertUploadsStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.gasUploadMap).thenReturn(
                mapOf(1 to CertificateUpload(1L, "cert.pdf")),
            )
            stubUpload(1L, FileUploadStatus.QUARANTINED)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val certRows = factory.createCertRows()
            assertEquals(
                listOf(
                    UploadedFileUrl(
                        messageKey = "propertyCompliance.uploadedFile.virusScanPendingWithName",
                        displayName = "cert.pdf",
                        url = null,
                    ),
                ),
                certRows[2].fieldValue,
            )
        }

        @Test
        fun `factory filters out deleted uploads`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(LocalDate(2024, 6, 15))
            whenever(mockState.gasCertIssueDateStep).thenReturn(mockGasCertIssueDateStep)
            whenever(mockState.checkGasCertUploadsStep).thenReturn(mockCheckGasCertUploadsStep)
            whenever(mockGasCertIssueDateStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockCheckGasCertUploadsStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.gasUploadMap).thenReturn(
                mapOf(
                    1 to CertificateUpload(1L, "scanned.pdf"),
                    2 to CertificateUpload(2L, "deleted.pdf"),
                ),
            )
            stubUpload(1L, FileUploadStatus.SCANNED, downloadUrl = "/download/scanned.pdf")
            stubUpload(2L, FileUploadStatus.DELETED)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val certRows = factory.createCertRows()
            val files = certRows[2].fieldValue as List<*>
            assertEquals(1, files.size)
            assertEquals("scanned.pdf", (files[0] as UploadedFileUrl).displayName)
        }

        @Test
        fun `factory preserves map index ordering across mixed status uploads`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(LocalDate(2024, 6, 15))
            whenever(mockState.gasCertIssueDateStep).thenReturn(mockGasCertIssueDateStep)
            whenever(mockState.checkGasCertUploadsStep).thenReturn(mockCheckGasCertUploadsStep)
            whenever(mockGasCertIssueDateStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockCheckGasCertUploadsStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.gasUploadMap).thenReturn(
                mapOf(
                    3 to CertificateUpload(3L, "third.pdf"),
                    1 to CertificateUpload(1L, "first.pdf"),
                    2 to CertificateUpload(2L, "second.pdf"),
                ),
            )
            stubUpload(1L, FileUploadStatus.SCANNED, downloadUrl = "/download/first.pdf")
            stubUpload(2L, FileUploadStatus.QUARANTINED)
            stubUpload(3L, FileUploadStatus.SCANNED, downloadUrl = "/download/third.pdf")

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val certRows = factory.createCertRows()
            val files = certRows[2].fieldValue as List<*>
            assertEquals(listOf("first.pdf", "second.pdf", "third.pdf"), files.map { (it as UploadedFileUrl).displayName })
            assertEquals("/download/first.pdf", (files[0] as UploadedFileUrl).url)
            assertNull((files[1] as UploadedFileUrl).url)
            assertEquals("/download/third.pdf", (files[2] as UploadedFileUrl).url)
        }
    }

    @Nested
    inner class ProvideLater {
        @Test
        fun `factory returns correct content for provide this later when occupied`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(true)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.occupied", gasSupplyRows[1].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for provide this later when unoccupied`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(false)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.unoccupied", gasSupplyRows[1].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(factory.getInsetTextKey())
        }
    }

    @Nested
    inner class NoCert {
        @Test
        fun `factory returns correct content for no cert when occupied`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(true)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals(false, gasSupplyRows[1].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertEquals("checkGasSafety.occupiedNoCertInsetText", factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for no cert when unoccupied`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(false)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.unoccupied", gasSupplyRows[1].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(factory.getInsetTextKey())
        }
    }

    @Nested
    inner class CertExpired {
        @Test
        fun `factory returns correct content for expired cert when occupied`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(true)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals(false, gasSupplyRows[1].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertEquals("checkGasSafety.occupiedNoCertInsetText", factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for expired cert when unoccupied`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(false)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.unoccupied", gasSupplyRows[1].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(factory.getInsetTextKey())
        }
    }
}
