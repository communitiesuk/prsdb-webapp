package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService
import uk.gov.communities.prsdb.webapp.services.VirusScanCallbackService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class UploadElectricalCertStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyState

    @Mock
    lateinit var virusScanCallbackService: VirusScanCallbackService

    @Mock
    lateinit var fileUploadCookieService: FileUploadCookieService

    @Mock
    lateinit var memberIdService: CollectionKeyParameterService

    @Mock
    lateinit var uploadElectricalCertStep: UploadElectricalCertStep

    @Test
    fun `getStepSpecificContent returns EIC heading when EIC is selected`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getElectricalCertificateType()).thenReturn(HasElectricalSafetyCertificate.HAS_EIC)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("forms.uploadCertificate.eic.fieldSetHeading", content["fieldSetHeading"])
        verify(fileUploadCookieService).addFileUploadCookieToResponse()
    }

    @Test
    fun `getStepSpecificContent returns EICR heading when EICR is selected`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getElectricalCertificateType()).thenReturn(HasElectricalSafetyCertificate.HAS_EICR)

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("forms.uploadCertificate.eicr.fieldSetHeading", content["fieldSetHeading"])
    }

    @Test
    fun `getStepSpecificContent throws exception when certificate type is null`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getElectricalCertificateType()).thenReturn(null)

        assertThrows<PrsdbWebException> {
            stepConfig.getStepSpecificContent(mockState)
        }
    }

    @Test
    fun `mode returns COMPLETE when electricalUploadMap is non-empty`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.electricalUploadMap).thenReturn(mapOf(1 to CertificateUpload(1L, "cert.pdf")))

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns null when electricalUploadMap is empty`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.electricalUploadMap).thenReturn(mapOf())

        assertNull(stepConfig.mode(mockState))
    }

    @Test
    fun `afterStepDataIsAdded updates the upload map and triggers virus scan callbacks`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(UploadElectricalCertStep.ROUTE_SEGMENT)).thenReturn(
            mapOf("name" to "cert.pdf", "fileUploadId" to "42"),
        )
        whenever(mockState.electricalUploadMap).thenReturn(mapOf())
        whenever(mockState.getNextElectricalUploadMemberId()).thenReturn(1)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.uploadElectricalCertStep).thenReturn(uploadElectricalCertStep)
        whenever(memberIdService.getParameterOrNull()).thenReturn(null)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(virusScanCallbackService).saveEmailForJourney("test-journey-id", 42L, CertificateType.Eicr)
        verify(virusScanCallbackService).saveEmailToMonitoringTeam("test-journey-id", 42L, CertificateType.Eicr)

        val updatedMapCaptor = argumentCaptor<Map<Int, CertificateUpload>>()
        verify(mockState).electricalUploadMap = updatedMapCaptor.capture()
        assertEquals(CertificateUpload(42L, "cert.pdf"), updatedMapCaptor.firstValue[1])
        verify(mockState).highestAssignedElectricalMemberId = 1
        verify(uploadElectricalCertStep).clearFormData()
    }

    @Test
    fun `afterStepDataIsAdded updates existing entry when memberIdService returns a key`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(UploadElectricalCertStep.ROUTE_SEGMENT)).thenReturn(
            mapOf("name" to "updated.pdf", "fileUploadId" to "55"),
        )
        whenever(mockState.electricalUploadMap).thenReturn(mapOf(3 to CertificateUpload(10L, "old.pdf")))
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.uploadElectricalCertStep).thenReturn(uploadElectricalCertStep)
        whenever(memberIdService.getParameterOrNull()).thenReturn(3)

        stepConfig.afterStepDataIsAdded(mockState)

        val updatedMapCaptor = argumentCaptor<Map<Int, CertificateUpload>>()
        verify(mockState).electricalUploadMap = updatedMapCaptor.capture()
        assertEquals(CertificateUpload(55L, "updated.pdf"), updatedMapCaptor.firstValue[3])
        verify(mockState).highestAssignedElectricalMemberId = 3
    }

    private fun setupStepConfig(): UploadElectricalCertStepConfig {
        val stepConfig = UploadElectricalCertStepConfig(virusScanCallbackService, fileUploadCookieService, memberIdService)
        stepConfig.routeSegment = UploadElectricalCertStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
