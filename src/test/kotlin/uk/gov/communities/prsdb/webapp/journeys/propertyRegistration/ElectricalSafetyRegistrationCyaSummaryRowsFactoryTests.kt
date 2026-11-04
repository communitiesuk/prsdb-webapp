package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.DateTimeUnit.Companion.YEAR
import kotlinx.datetime.plus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicrUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.UploadedFileUrl
import uk.gov.communities.prsdb.webapp.services.UploadService

@ExtendWith(MockitoExtension::class)
class ElectricalSafetyRegistrationCyaSummaryRowsFactoryTests {
    @Mock
    lateinit var mockState: ElectricalSafetyDetailState

    private val mockHasElectricalCertStep: HasElectricalCertStep = mock()

    // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
    private val mockEicExpiryDateStep: EicExpiryDateStep = mock()
    private val mockEicIssueDateStep: EicIssueDateStep = mock()
    private val mockCheckEicUploadsStep: CheckEicUploadsStep = mock()
    private val mockEicrExpiryDateStep: EicrExpiryDateStep = mock()
    private val mockCheckEicrUploadsStep: CheckEicrUploadsStep = mock()
    private val mockUploadService: UploadService = mock()
    private val inDateExpiryDate = DateTimeHelper().getCurrentDateInUK().plus(1, YEAR)

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
        whenever(mockState.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)
        whenever(mockHasElectricalCertStep.currentJourneyId).thenReturn("test-journey-id")
    }

    // TODO PDJB-1679: Remove isEicIssueDateEnabled and always stub the EIC issue date step when the EIC_ISSUE_DATE flag is removed
    private fun setupEicCertUploadedMocks(isEicIssueDateEnabled: Boolean) {
        setupCommonStateMocks()
        whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EIC)
        whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)
        whenever(mockState.getElectricalCertificateType()).thenReturn(HasElectricalSafetyCertificate.HAS_EIC)
        whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(inDateExpiryDate)
        whenever(mockState.isEicIssueDateEnabled).thenReturn(isEicIssueDateEnabled)
        if (isEicIssueDateEnabled) {
            whenever(mockState.eicIssueDateStep).thenReturn(mockEicIssueDateStep)
            whenever(mockEicIssueDateStep.currentJourneyId).thenReturn("test-journey-id")
        } else {
            whenever(mockState.eicExpiryDateStep).thenReturn(mockEicExpiryDateStep)
            whenever(mockEicExpiryDateStep.currentJourneyId).thenReturn("test-journey-id")
        }
        whenever(mockState.checkEicUploadsStep).thenReturn(mockCheckEicUploadsStep)
        whenever(mockCheckEicUploadsStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockState.eicUploadMap).thenReturn(mapOf(1 to CertificateUpload(1L, "cert.pdf")))
        stubUpload(1L, FileUploadStatus.SCANNED, downloadUrl = "/download/cert.pdf")
    }

    @Nested
    inner class CertUploaded {
        // TODO PDJB-1679: Use the EIC issue date step instead of the EIC expiry date step when the EIC_ISSUE_DATE flag is removed
        @Test
        fun `factory uses the EIC branch steps and uploads, wires up EIC download messageKey and sorts uploads by map index`() {
            whenever(mockState.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EIC)
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)
            whenever(mockState.getElectricalCertificateType()).thenReturn(HasElectricalSafetyCertificate.HAS_EIC)

            whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(inDateExpiryDate)
            whenever(mockState.isEicIssueDateEnabled).thenReturn(false)

            whenever(mockState.eicExpiryDateStep).thenReturn(mockEicExpiryDateStep)
            whenever(mockState.checkEicUploadsStep).thenReturn(mockCheckEicUploadsStep)
            whenever(mockState.eicUploadMap).thenReturn(
                mapOf(
                    2 to CertificateUpload(2L, "second.pdf"),
                    1 to CertificateUpload(1L, "first.pdf"),
                ),
            )
            stubUpload(1L, FileUploadStatus.SCANNED, downloadUrl = "/download/first.pdf")
            stubUpload(2L, FileUploadStatus.SCANNED, downloadUrl = "/download/second.pdf")

            val requestedSteps = mutableListOf<JourneyStep.RequestableStep<*, *, *>>()
            val factory =
                ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService) {
                    requestedSteps.add(it)
                    Destination.Nowhere()
                }

            val rows = factory.createRows()
            assertEquals(3, rows.size)
            assertEquals(listOf(mockHasElectricalCertStep, mockEicExpiryDateStep, mockCheckEicUploadsStep), requestedSteps)
            assertEquals("checkElectricalSafety.eicLabel", rows[0].fieldValue)
            assertEquals(inDateExpiryDate, rows[1].fieldValue)
            assertEquals(
                listOf(
                    UploadedFileUrl(
                        messageKey = "propertyDetails.complianceInformation.electricalSafety.eic.downloadCertificate",
                        displayName = "first.pdf",
                        url = "/download/first.pdf",
                    ),
                    UploadedFileUrl(
                        messageKey = "propertyDetails.complianceInformation.electricalSafety.eic.downloadCertificate",
                        displayName = "second.pdf",
                        url = "/download/second.pdf",
                    ),
                ),
                rows[2].fieldValue,
            )

            assertNull(factory.getInsetTextKey())
        }

        @Test
        fun `factory links the EIC date row to the EIC issue date step with a blank value when the EIC issue date flag is enabled`() {
            whenever(mockState.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EIC)
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)
            whenever(mockState.getElectricalCertificateType()).thenReturn(HasElectricalSafetyCertificate.HAS_EIC)
            // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
            whenever(mockState.isEicIssueDateEnabled).thenReturn(true)
            whenever(mockState.eicIssueDateStep).thenReturn(mockEicIssueDateStep)
            whenever(mockState.checkEicUploadsStep).thenReturn(mockCheckEicUploadsStep)
            whenever(mockState.eicUploadMap).thenReturn(mapOf(1 to CertificateUpload(1L, "cert.pdf")))
            stubUpload(1L, FileUploadStatus.SCANNED, downloadUrl = "/download/cert.pdf")

            val requestedSteps = mutableListOf<JourneyStep.RequestableStep<*, *, *>>()
            val factory =
                ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService) {
                    requestedSteps.add(it)
                    Destination.Nowhere()
                }

            val rows = factory.createRows()
            assertEquals(listOf(mockHasElectricalCertStep, mockEicIssueDateStep, mockCheckEicUploadsStep), requestedSteps)
            assertEquals("", rows[1].fieldValue)
        }

        @Test
        fun `factory uses the issue date heading for an EIC when EIC issue date is enabled`() {
            setupEicCertUploadedMocks(isEicIssueDateEnabled = true)

            val rows =
                ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService).createRows()

            assertEquals("checkElectricalSafety.issueDate.fieldHeading", rows[1].fieldHeading)
        }

        // TODO PDJB-1679: Remove this test when the EIC_ISSUE_DATE flag is removed
        @Test
        fun `factory uses the expiry date heading for an EIC when EIC issue date is disabled`() {
            setupEicCertUploadedMocks(isEicIssueDateEnabled = false)

            val rows =
                ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService).createRows()

            assertEquals("checkElectricalSafety.expiryDate.fieldHeading", rows[1].fieldHeading)
        }

        @Test
        fun `factory wires up EICR download messageKey and uses the expiry date heading even when EIC issue date is enabled`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EICR)
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)
            whenever(mockState.getElectricalCertificateType()).thenReturn(HasElectricalSafetyCertificate.HAS_EICR)
            // Lenient because the EICR branch shouldn't read the flag at all
            // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
            lenient().`when`(mockState.isEicIssueDateEnabled).thenReturn(true)

            whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(inDateExpiryDate)

            whenever(mockState.eicrExpiryDateStep).thenReturn(mockEicrExpiryDateStep)
            whenever(mockState.checkEicrUploadsStep).thenReturn(mockCheckEicrUploadsStep)
            whenever(mockEicrExpiryDateStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockCheckEicrUploadsStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.eicrUploadMap).thenReturn(
                mapOf(1 to CertificateUpload(1L, "cert.pdf")),
            )
            stubUpload(1L, FileUploadStatus.SCANNED, downloadUrl = "/download/cert.pdf")

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val rows = factory.createRows()
            assertEquals(3, rows.size)
            assertEquals("checkElectricalSafety.eicrLabel", rows[0].fieldValue)
            assertEquals("checkElectricalSafety.expiryDate.fieldHeading", rows[1].fieldHeading)
            assertEquals(
                listOf(
                    UploadedFileUrl(
                        messageKey = "propertyDetails.complianceInformation.electricalSafety.eicr.downloadCertificate",
                        displayName = "cert.pdf",
                        url = "/download/cert.pdf",
                    ),
                ),
                rows[2].fieldValue,
            )
        }
    }

    @Nested
    inner class ProvideLater {
        @Test
        fun `factory returns correct content for provide this later when occupied`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(true)

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val rows = factory.createRows()
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.provideThisLater.occupied", rows[0].fieldValue)

            assertNull(factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for provide this later when unoccupied`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(false)

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val rows = factory.createRows()
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.provideThisLater.unoccupied", rows[0].fieldValue)

            assertNull(factory.getInsetTextKey())
        }
    }

    @Nested
    inner class NoCert {
        @Test
        fun `factory returns correct content for no cert when occupied`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(true)

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val rows = factory.createRows()
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.noneLabel", rows[0].fieldValue)

            assertEquals("checkElectricalSafety.occupiedNoCertInsetText", factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for no cert when unoccupied`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(false)

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val rows = factory.createRows()
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.provideThisLater.unoccupied", rows[0].fieldValue)

            assertNull(factory.getInsetTextKey())
        }
    }

    @Nested
    inner class CertExpired {
        @Test
        fun `factory returns correct content for expired cert when occupied`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EIC)
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(true)

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val rows = factory.createRows()
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.noneLabel", rows[0].fieldValue)

            assertEquals("checkElectricalSafety.occupiedNoCertInsetText", factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for expired cert when unoccupied`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EICR)
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(false)

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState, mockUploadService)

            val rows = factory.createRows()
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.provideThisLater.unoccupied", rows[0].fieldValue)

            assertNull(factory.getInsetTextKey())
        }
    }
}
