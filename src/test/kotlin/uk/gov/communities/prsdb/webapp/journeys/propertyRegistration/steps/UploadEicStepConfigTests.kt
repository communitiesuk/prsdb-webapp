package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService
import uk.gov.communities.prsdb.webapp.services.VirusScanCallbackService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class UploadEicStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyDetailState

    @Mock
    lateinit var virusScanCallbackService: VirusScanCallbackService

    @Mock
    lateinit var fileUploadCookieService: FileUploadCookieService

    @Mock
    lateinit var memberIdService: CollectionKeyParameterService

    @Mock
    lateinit var uploadEicStep: UploadEicStep

    @Test
    fun `getStepSpecificContent returns EIC heading and adds file upload cookie`() {
        val stepConfig = setupStepConfig()

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("forms.uploadCertificate.eic.fieldSetHeading", content["fieldSetHeading"])
        verify(fileUploadCookieService).addFileUploadCookieToResponse()
    }

    @Test
    fun `mode returns COMPLETE when eicUploadMap is non-empty`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.eicUploadMap).thenReturn(mapOf(1 to CertificateUpload(1L, "cert.pdf")))

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns null when eicUploadMap is empty`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.eicUploadMap).thenReturn(mapOf())

        assertNull(stepConfig.mode(mockState))
    }

    @Test
    fun `afterStepDataIsAdded saves an EIC virus scan email and adds the upload to the EIC upload map`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(UploadEicStep.ROUTE_SEGMENT)).thenReturn(
            mapOf("name" to "cert.pdf", "fileUploadId" to "42"),
        )
        whenever(mockState.eicUploadMap).thenReturn(mapOf())
        whenever(mockState.getNextEicUploadMemberId()).thenReturn(1)
        whenever(memberIdService.getParameterOrNull()).thenReturn(null)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.uploadEicStep).thenReturn(uploadEicStep)
        whenever(mockState.propertyOwnershipId).thenReturn(99L)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(virusScanCallbackService).saveVirusScanFailureEmail(
            journeyId = "test-journey-id",
            fileUploadId = 42L,
            certificateType = CertificateType.Eic,
            propertyOwnershipId = 99L,
        )
        val updatedMapCaptor = argumentCaptor<Map<Int, CertificateUpload>>()
        verify(mockState).eicUploadMap = updatedMapCaptor.capture()
        assertEquals(CertificateUpload(42L, "cert.pdf"), updatedMapCaptor.firstValue[1])
        verify(mockState).highestAssignedEicMemberId = 1
        verify(uploadEicStep).clearFormData()
    }

    @Test
    fun `afterStepDataIsAdded updates existing entry when memberIdService returns a key`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(UploadEicStep.ROUTE_SEGMENT)).thenReturn(
            mapOf("name" to "updated.pdf", "fileUploadId" to "55"),
        )
        whenever(mockState.eicUploadMap).thenReturn(mapOf(3 to CertificateUpload(10L, "old.pdf")))
        whenever(memberIdService.getParameterOrNull()).thenReturn(3)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.uploadEicStep).thenReturn(uploadEicStep)
        whenever(mockState.propertyOwnershipId).thenReturn(null)

        stepConfig.afterStepDataIsAdded(mockState)

        val updatedMapCaptor = argumentCaptor<Map<Int, CertificateUpload>>()
        verify(mockState).eicUploadMap = updatedMapCaptor.capture()
        assertEquals(CertificateUpload(55L, "updated.pdf"), updatedMapCaptor.firstValue[3])
        verify(mockState).highestAssignedEicMemberId = 3
    }

    private fun setupStepConfig(): UploadEicStepConfig {
        val stepConfig =
            UploadEicStepConfig(
                virusScanCallbackService,
                fileUploadCookieService,
                memberIdService,
            )
        stepConfig.urlPath = UploadEicStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
