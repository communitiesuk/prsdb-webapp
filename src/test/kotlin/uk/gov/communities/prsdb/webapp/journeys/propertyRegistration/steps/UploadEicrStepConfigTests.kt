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
class UploadEicrStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyDetailState

    @Mock
    lateinit var virusScanCallbackService: VirusScanCallbackService

    @Mock
    lateinit var fileUploadCookieService: FileUploadCookieService

    @Mock
    lateinit var memberIdService: CollectionKeyParameterService

    @Mock
    lateinit var uploadElectricalCertStep: UploadEicrStep

    @Test
    fun `getStepSpecificContent returns EICR heading and adds file upload cookie`() {
        val stepConfig = setupStepConfig()

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("forms.uploadCertificate.eicr.fieldSetHeading", content["fieldSetHeading"])
        verify(fileUploadCookieService).addFileUploadCookieToResponse()
    }

    @Test
    fun `mode returns COMPLETE when eicrUploadMap is non-empty`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.eicrUploadMap).thenReturn(mapOf(1 to CertificateUpload(1L, "cert.pdf")))

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns null when eicrUploadMap is empty`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.eicrUploadMap).thenReturn(mapOf())

        assertNull(stepConfig.mode(mockState))
    }

    @Test
    fun `afterStepDataIsAdded uses the landlord id when property ownership id is not known`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(UploadEicrStep.ROUTE_SEGMENT)).thenReturn(
            mapOf("name" to "cert.pdf", "fileUploadId" to "42"),
        )
        whenever(mockState.eicrUploadMap).thenReturn(mapOf())
        whenever(mockState.getNextEicrUploadMemberId()).thenReturn(1)
        whenever(memberIdService.getParameterOrNull()).thenReturn(null)
        stubStateForAfterStepDataIsAdded()

        stepConfig.afterStepDataIsAdded(mockState)

        verify(virusScanCallbackService).saveVirusScanFailureEmail(
            journeyId = "test-journey-id",
            fileUploadId = 42L,
            certificateType = CertificateType.Eicr,
            propertyOwnershipId = null,
        )

        val updatedMapCaptor = argumentCaptor<Map<Int, CertificateUpload>>()
        verify(mockState).eicrUploadMap = updatedMapCaptor.capture()
        assertEquals(CertificateUpload(42L, "cert.pdf"), updatedMapCaptor.firstValue[1])
        verify(mockState).highestAssignedEicrMemberId = 1
        verify(uploadElectricalCertStep).clearFormData()
    }

    @Test
    fun `afterStepDataIsAdded uses the property ownership id when known`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(UploadEicrStep.ROUTE_SEGMENT)).thenReturn(
            mapOf("name" to "cert.pdf", "fileUploadId" to "42"),
        )
        whenever(mockState.eicrUploadMap).thenReturn(mapOf())
        whenever(mockState.getNextEicrUploadMemberId()).thenReturn(1)
        whenever(memberIdService.getParameterOrNull()).thenReturn(null)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.uploadEicrStep).thenReturn(uploadElectricalCertStep)
        whenever(mockState.propertyOwnershipId).thenReturn(99L)
        stepConfig.afterStepDataIsAdded(mockState)

        verify(virusScanCallbackService).saveVirusScanFailureEmail(
            journeyId = "test-journey-id",
            fileUploadId = 42L,
            certificateType = CertificateType.Eicr,
            propertyOwnershipId = 99L,
        )
    }

    @Test
    fun `afterStepDataIsAdded updates existing entry when memberIdService returns a key`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(UploadEicrStep.ROUTE_SEGMENT)).thenReturn(
            mapOf("name" to "updated.pdf", "fileUploadId" to "55"),
        )
        whenever(mockState.eicrUploadMap).thenReturn(mapOf(3 to CertificateUpload(10L, "old.pdf")))
        whenever(memberIdService.getParameterOrNull()).thenReturn(3)
        stubStateForAfterStepDataIsAdded()

        stepConfig.afterStepDataIsAdded(mockState)

        val updatedMapCaptor = argumentCaptor<Map<Int, CertificateUpload>>()
        verify(mockState).eicrUploadMap = updatedMapCaptor.capture()
        assertEquals(CertificateUpload(55L, "updated.pdf"), updatedMapCaptor.firstValue[3])
        verify(mockState).highestAssignedEicrMemberId = 3
    }

    private fun stubStateForAfterStepDataIsAdded() {
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.uploadEicrStep).thenReturn(uploadElectricalCertStep)
        whenever(mockState.propertyOwnershipId).thenReturn(null)
    }

    private fun setupStepConfig(): UploadEicrStepConfig {
        val stepConfig =
            UploadEicrStepConfig(
                virusScanCallbackService,
                fileUploadCookieService,
                memberIdService,
            )
        stepConfig.urlPath = UploadEicrStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
