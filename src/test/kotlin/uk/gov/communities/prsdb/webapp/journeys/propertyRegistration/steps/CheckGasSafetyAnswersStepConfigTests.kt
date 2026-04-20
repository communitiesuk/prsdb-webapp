package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.viewModels.DownloadableFileViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class CheckGasSafetyAnswersStepConfigTests {
    @Mock
    lateinit var mockState: GasSafetyState

    @Mock
    lateinit var mockUploadService: UploadService

    private val mockHasGasSupplyStep: HasGasSupplyStep = mock()
    private val mockHasGasCertStep: HasGasCertStep = mock()
    private val mockGasCertIssueDateStep: GasCertIssueDateStep = mock()
    private val mockCheckGasCertUploadsStep: CheckGasCertUploadsStep = mock()

    private fun setupStepConfig(): CheckGasSafetyAnswersStepConfig {
        val stepConfig = CheckGasSafetyAnswersStepConfig(mockUploadService)
        stepConfig.routeSegment = CheckGasSafetyAnswersStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    private fun setupCommonStateMocks() {
        whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
        whenever(mockState.hasGasCertStep).thenReturn(mockHasGasCertStep)
        whenever(mockHasGasSupplyStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockHasGasCertStep.currentJourneyId).thenReturn("test-journey-id")
    }

    @Suppress("UNCHECKED_CAST")
    private fun getGasSupplyRows(content: Map<String, Any?>): List<SummaryListRowViewModel> =
        content["gasSupplyRows"] as List<SummaryListRowViewModel>

    @Suppress("UNCHECKED_CAST")
    private fun getCertRows(content: Map<String, Any?>): List<SummaryListRowViewModel> =
        content["certRows"] as List<SummaryListRowViewModel>

    @Test
    fun `chooseTemplate returns checkGasSafetyAnswersForm`() {
        val stepConfig = setupStepConfig()
        val result = stepConfig.chooseTemplate(mockState)
        assertEquals("forms/checkGasSafetyAnswersForm", result)
    }

    @Test
    fun `mode returns COMPLETE when form data exists`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(CheckGasSafetyAnswersStep.ROUTE_SEGMENT)).thenReturn(mapOf("key" to "value"))

        val result = stepConfig.mode(mockState)

        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `mode returns null when no form data exists`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(CheckGasSafetyAnswersStep.ROUTE_SEGMENT)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Nested
    inner class NoGasSupply {
        @Test
        fun `getStepSpecificContent returns correct content when there is no gas supply`() {
            // Arrange
            val stepConfig = setupStepConfig()
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockHasGasSupplyStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.NO)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(1, gasSupplyRows.size)
            assertEquals(false, gasSupplyRows[0].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertEquals("checkGasSafety.noGasSupplyInsetText", content["insetTextKey"])
            assertEquals("forms.buttons.saveAndContinue", content["submitButtonText"])
        }
    }

    @Nested
    inner class UploadedCertificate {
        @Test
        fun `getStepSpecificContent returns correct content for valid cert with uploads`() {
            // Arrange
            val stepConfig = setupStepConfig()
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

            val mockFileUpload: FileUpload = mock()
            whenever(mockUploadService.getFileUploadById(1L)).thenReturn(mockFileUpload)
            whenever(mockUploadService.getDownloadUrlOrNull(mockFileUpload, "cert.pdf")).thenReturn("https://s3.example.com/cert.pdf")

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(1, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(3, certRows.size)
            assertEquals(true, certRows[0].fieldValue)
            assertEquals(issueDate, certRows[1].fieldValue)
            val uploadValues = certRows[2].fieldValue as List<*>
            assertEquals(1, uploadValues.size)
            val downloadableFile = uploadValues[0] as DownloadableFileViewModel
            assertEquals("cert.pdf", downloadableFile.fileName)
            assertEquals("https://s3.example.com/cert.pdf", downloadableFile.downloadUrl)

            assertNull(content["insetTextKey"])
            assertEquals("forms.buttons.saveAndContinue", content["submitButtonText"])
        }

        @Test
        fun `getStepSpecificContent sorts uploads by map key and returns file names`() {
            // Arrange
            val stepConfig = setupStepConfig()
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

            val mockFileUpload1: FileUpload = mock()
            val mockFileUpload2: FileUpload = mock()
            val mockFileUpload3: FileUpload = mock()
            whenever(mockUploadService.getFileUploadById(1L)).thenReturn(mockFileUpload1)
            whenever(mockUploadService.getFileUploadById(2L)).thenReturn(mockFileUpload2)
            whenever(mockUploadService.getFileUploadById(3L)).thenReturn(mockFileUpload3)
            whenever(mockUploadService.getDownloadUrlOrNull(mockFileUpload1, "first.pdf")).thenReturn("https://s3.example.com/first.pdf")
            whenever(mockUploadService.getDownloadUrlOrNull(mockFileUpload2, "second.pdf")).thenReturn("https://s3.example.com/second.pdf")
            whenever(mockUploadService.getDownloadUrlOrNull(mockFileUpload3, "third.pdf")).thenReturn("https://s3.example.com/third.pdf")

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val certRows = getCertRows(content)
            val uploadValues = certRows[2].fieldValue as List<*>
            assertEquals(3, uploadValues.size)
            assertEquals("first.pdf", (uploadValues[0] as DownloadableFileViewModel).fileName)
            assertEquals("second.pdf", (uploadValues[1] as DownloadableFileViewModel).fileName)
            assertEquals("third.pdf", (uploadValues[2] as DownloadableFileViewModel).fileName)
        }

        @Test
        fun `getStepSpecificContent returns null downloadUrl when file is not yet downloadable`() {
            // Arrange
            val stepConfig = setupStepConfig()
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
                mapOf(1 to CertificateUpload(1L, "scanning.pdf")),
            )

            val mockFileUpload: FileUpload = mock()
            whenever(mockUploadService.getFileUploadById(1L)).thenReturn(mockFileUpload)
            whenever(mockUploadService.getDownloadUrlOrNull(mockFileUpload, "scanning.pdf")).thenReturn(null)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val certRows = getCertRows(content)
            val uploadValues = certRows[2].fieldValue as List<*>
            assertEquals(1, uploadValues.size)
            val downloadableFile = uploadValues[0] as DownloadableFileViewModel
            assertEquals("scanning.pdf", downloadableFile.fileName)
            assertNull(downloadableFile.downloadUrl)
        }
    }

    @Nested
    inner class ProvideLater {
        @Test
        fun `getStepSpecificContent returns correct content for provide this later when occupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(true)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.occupied", gasSupplyRows[1].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(content["insetTextKey"])
        }

        @Test
        fun `getStepSpecificContent returns correct content for provide this later when unoccupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(false)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.unoccupied", gasSupplyRows[1].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(content["insetTextKey"])
        }
    }

    @Nested
    inner class NoCert {
        @Test
        fun `getStepSpecificContent returns correct content for no cert when occupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(true)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals(false, gasSupplyRows[1].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertEquals("checkGasSafety.occupiedNoCertInsetText", content["insetTextKey"])
        }

        @Test
        fun `getStepSpecificContent returns correct content for no cert when unoccupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(false)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.unoccupied", gasSupplyRows[1].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(content["insetTextKey"])
        }
    }

    @Nested
    inner class CertExpired {
        @Test
        fun `getStepSpecificContent returns correct content for expired cert when occupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(true)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals(false, gasSupplyRows[1].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertEquals("checkGasSafety.occupiedNoCertInsetText", content["insetTextKey"])
        }

        @Test
        fun `getStepSpecificContent returns correct content for expired cert when unoccupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(false)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.unoccupied", gasSupplyRows[1].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(content["insetTextKey"])
        }
    }
}
