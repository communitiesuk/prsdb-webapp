package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class CheckEicUploadsStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyDetailState

    @Mock
    lateinit var mockMemberIdService: CollectionKeyParameterService

    @Mock
    lateinit var mockUploadService: UploadService

    @Mock
    lateinit var mockUploadEicStep: UploadEicStep

    @Mock
    lateinit var mockRemoveEicUploadStep: RemoveEicUploadStep

    private lateinit var stepConfig: CheckEicUploadsStepConfig

    @BeforeEach
    fun setup() {
        stepConfig = CheckEicUploadsStepConfig(mockMemberIdService, mockUploadService)
        stepConfig.urlPath = CheckEicUploadsStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
    }

    @Test
    fun `mode returns COMPLETE when eicUploadMap is not empty`() {
        whenever(mockState.eicUploadMap).thenReturn(mapOf(0 to CertificateUpload(1L, "eic.pdf")))

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns null when eicUploadMap is empty`() {
        whenever(mockState.eicUploadMap).thenReturn(emptyMap())

        assertNull(stepConfig.mode(mockState))
    }

    @Test
    fun `getStepSpecificContent returns upload rows from the EIC upload map`() {
        whenever(mockState.uploadEicStep).thenReturn(mockUploadEicStep)
        whenever(mockUploadEicStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockState.removeEicUploadStep).thenReturn(mockRemoveEicUploadStep)
        whenever(mockRemoveEicUploadStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockMemberIdService.createParameterPair(any())).thenReturn("memberId" to "0")
        whenever(mockState.eicUploadMap).thenReturn(mapOf(0 to CertificateUpload(1L, "eic.pdf")))

        val mockFileUpload = mock<FileUpload>()
        whenever(mockFileUpload.status).thenReturn(FileUploadStatus.SCANNED)
        whenever(mockUploadService.getFileUploadById(1L)).thenReturn(mockFileUpload)
        whenever(mockUploadService.getDownloadUrlOrNull(mockFileUpload, "eic.pdf")).thenReturn("/download/1")

        val content = stepConfig.getStepSpecificContent(mockState)

        @Suppress("UNCHECKED_CAST")
        val uploadRows = content["uploadRows"] as List<UploadRow>
        assertEquals(1, uploadRows.size)
        assertEquals("eic.pdf", uploadRows[0].fileName)
        assertEquals("/download/1", uploadRows[0].downloadUrl)
        assertEquals(1, content["optionalAddAnotherTitleParam"])
    }
}
