package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class CheckElectricalCertUploadsStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyState

    @Mock
    lateinit var mockMemberIdService: CollectionKeyParameterService

    @Mock
    lateinit var mockUploadService: UploadService

    @Mock
    lateinit var mockUploadElectricalCertStep: UploadElectricalCertStep

    @Mock
    lateinit var mockRemoveElectricalCertUploadStep: RemoveElectricalCertUploadStep

    private lateinit var stepConfig: CheckElectricalCertUploadsStepConfig

    @BeforeEach
    fun setup() {
        stepConfig = CheckElectricalCertUploadsStepConfig(mockMemberIdService, mockUploadService)
        stepConfig.routeSegment = CheckElectricalCertUploadsStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
    }

    @Nested
    inner class ChooseTemplate {
        @Test
        fun `chooseTemplate returns addAnotherFormWithFileUploadTable`() {
            val result = stepConfig.chooseTemplate(mockState)

            assertEquals("forms/addAnotherFormWithFileUploadTable", result)
        }
    }

    @Nested
    inner class Mode {
        @Test
        fun `mode returns COMPLETE when electricalUploadMap is not empty`() {
            whenever(mockState.electricalUploadMap).thenReturn(
                mapOf(0 to CertificateUpload(1L, "eicr.pdf")),
            )

            val result = stepConfig.mode(mockState)

            assertEquals(Complete.COMPLETE, result)
        }

        @Test
        fun `mode returns null when electricalUploadMap is empty`() {
            whenever(mockState.electricalUploadMap).thenReturn(emptyMap())

            val result = stepConfig.mode(mockState)

            assertNull(result)
        }
    }

    @Nested
    inner class GetStepSpecificContent {
        private fun setupStepMocks(includeRemoveStep: Boolean = true) {
            whenever(mockState.uploadElectricalCertStep).thenReturn(mockUploadElectricalCertStep)
            whenever(mockUploadElectricalCertStep.currentJourneyId).thenReturn("test-journey-id")
            if (includeRemoveStep) {
                whenever(mockState.removeElectricalCertUploadStep).thenReturn(mockRemoveElectricalCertUploadStep)
                whenever(mockRemoveElectricalCertUploadStep.currentJourneyId).thenReturn("test-journey-id")
            }
        }

        @Test
        fun `getStepSpecificContent returns upload rows for each upload in the map`() {
            setupStepMocks()
            val uploadMap =
                mapOf(
                    0 to CertificateUpload(1L, "eicr1.pdf"),
                    1 to CertificateUpload(2L, "eicr2.pdf"),
                )
            whenever(mockState.electricalUploadMap).thenReturn(uploadMap)

            val mockFileUpload1 = mock<FileUpload>()
            val mockFileUpload2 = mock<FileUpload>()
            whenever(mockFileUpload1.status).thenReturn(FileUploadStatus.SCANNED)
            whenever(mockFileUpload2.status).thenReturn(FileUploadStatus.SCANNED)
            whenever(mockUploadService.getFileUploadById(1L)).thenReturn(mockFileUpload1)
            whenever(mockUploadService.getFileUploadById(2L)).thenReturn(mockFileUpload2)
            whenever(mockUploadService.getDownloadUrlOrNull(mockFileUpload1, "eicr1.pdf")).thenReturn("/download/1")
            whenever(mockUploadService.getDownloadUrlOrNull(mockFileUpload2, "eicr2.pdf")).thenReturn("/download/2")
            whenever(mockMemberIdService.createParameterPair(0)).thenReturn("memberId" to "0")
            whenever(mockMemberIdService.createParameterPair(1)).thenReturn("memberId" to "1")

            val content = stepConfig.getStepSpecificContent(mockState)

            @Suppress("UNCHECKED_CAST")
            val uploadRows = content["uploadRows"] as List<UploadRow>
            assertEquals(2, uploadRows.size)
            assertEquals("eicr1.pdf", uploadRows[0].fileName)
            assertEquals("eicr2.pdf", uploadRows[1].fileName)
            assertEquals("/download/1", uploadRows[0].downloadUrl)
            assertEquals("/download/2", uploadRows[1].downloadUrl)
        }

        @Test
        fun `getStepSpecificContent returns correct upload count in title param`() {
            setupStepMocks()
            val uploadMap = mapOf(0 to CertificateUpload(1L, "eicr.pdf"))
            whenever(mockState.electricalUploadMap).thenReturn(uploadMap)

            val mockFileUpload = mock<FileUpload>()
            whenever(mockFileUpload.status).thenReturn(FileUploadStatus.SCANNED)
            whenever(mockUploadService.getFileUploadById(1L)).thenReturn(mockFileUpload)
            whenever(mockUploadService.getDownloadUrlOrNull(mockFileUpload, "eicr.pdf")).thenReturn("/download/1")
            whenever(mockMemberIdService.createParameterPair(0)).thenReturn("memberId" to "0")

            val content = stepConfig.getStepSpecificContent(mockState)

            assertEquals(1, content["optionalAddAnotherTitleParam"])
        }

        @Test
        fun `getStepSpecificContent returns empty upload rows when map is empty`() {
            setupStepMocks(includeRemoveStep = false)
            whenever(mockState.electricalUploadMap).thenReturn(emptyMap())

            val content = stepConfig.getStepSpecificContent(mockState)

            @Suppress("UNCHECKED_CAST")
            val uploadRows = content["uploadRows"] as List<UploadRow>
            assertEquals(0, uploadRows.size)
            assertEquals(0, content["optionalAddAnotherTitleParam"])
        }
    }
}
