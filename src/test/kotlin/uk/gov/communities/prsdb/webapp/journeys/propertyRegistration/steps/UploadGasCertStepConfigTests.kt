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
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.services.VirusScanCallbackService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class UploadGasCertStepConfigTests {
    @Mock
    lateinit var mockState: GasSafetyDetailState

    @Mock
    lateinit var virusScanCallbackService: VirusScanCallbackService

    @Mock
    lateinit var fileUploadCookieService: FileUploadCookieService

    @Mock
    lateinit var memberIdService: CollectionKeyParameterService

    @Mock
    lateinit var userToLandlordService: UserToLandlordService

    @Mock
    lateinit var landlord: Landlord

    @Mock
    lateinit var uploadGasCertStep: UploadGasCertStep

    @Test
    fun `getStepSpecificContent returns the gas safety heading`() {
        val stepConfig = setupStepConfig()

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("forms.uploadCertificate.gasSafety.fieldSetHeading", content["fieldSetHeading"])
        verify(fileUploadCookieService).addFileUploadCookieToResponse()
    }

    @Test
    fun `mode returns COMPLETE when gasUploadMap is non-empty`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.gasUploadMap).thenReturn(mapOf(1 to CertificateUpload(1L, "cert.pdf")))

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns null when gasUploadMap is empty`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.gasUploadMap).thenReturn(mapOf())

        assertNull(stepConfig.mode(mockState))
    }

    @Test
    fun `afterStepDataIsAdded updates the upload map and triggers virus scan callbacks`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(UploadGasCertStep.ROUTE_SEGMENT)).thenReturn(
            mapOf("name" to "cert.pdf", "fileUploadId" to "42"),
        )
        whenever(mockState.gasUploadMap).thenReturn(mapOf())
        whenever(mockState.getNextGasUploadMemberId()).thenReturn(1)
        whenever(memberIdService.getParameterOrNull()).thenReturn(null)
        stubStateForAfterStepDataIsAdded()

        stepConfig.afterStepDataIsAdded(mockState)

        verify(virusScanCallbackService).saveVirusScanFailureEmail(
            journeyId = "test-journey-id",
            fileUploadId = 42L,
            certificateType = CertificateType.GasSafetyCert,
            propertyOwnershipId = null,
            landlordId = 7L,
        )

        val updatedMapCaptor = argumentCaptor<Map<Int, CertificateUpload>>()
        verify(mockState).gasUploadMap = updatedMapCaptor.capture()
        assertEquals(CertificateUpload(42L, "cert.pdf"), updatedMapCaptor.firstValue[1])
        verify(mockState).highestAssignedGasMemberId = 1
        verify(uploadGasCertStep).clearFormData()
    }

    @Test
    fun `afterStepDataIsAdded uses the property ownership id when the upload is on an update journey`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(UploadGasCertStep.ROUTE_SEGMENT)).thenReturn(
            mapOf("name" to "cert.pdf", "fileUploadId" to "42"),
        )
        whenever(mockState.gasUploadMap).thenReturn(mapOf())
        whenever(mockState.getNextGasUploadMemberId()).thenReturn(1)
        whenever(memberIdService.getParameterOrNull()).thenReturn(null)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.uploadGasCertStep).thenReturn(uploadGasCertStep)
        whenever(mockState.propertyOwnershipId).thenReturn(99L)
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
        whenever(landlord.id).thenReturn(7L)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(virusScanCallbackService).saveVirusScanFailureEmail(
            journeyId = "test-journey-id",
            fileUploadId = 42L,
            certificateType = CertificateType.GasSafetyCert,
            propertyOwnershipId = 99L,
            landlordId = 7L,
        )
    }

    @Test
    fun `afterStepDataIsAdded throws when there is no property ownership id and no acting landlord`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(UploadGasCertStep.ROUTE_SEGMENT)).thenReturn(
            mapOf("name" to "cert.pdf", "fileUploadId" to "42"),
        )
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.propertyOwnershipId).thenReturn(null)
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenThrow(
            ResponseStatusException(HttpStatus.BAD_REQUEST, "No landlord was found for user"),
        )

        assertThrows<ResponseStatusException> {
            stepConfig.afterStepDataIsAdded(mockState)
        }
    }

    @Test
    fun `afterStepDataIsAdded updates existing entry when memberIdService returns a key`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(UploadGasCertStep.ROUTE_SEGMENT)).thenReturn(
            mapOf("name" to "updated.pdf", "fileUploadId" to "55"),
        )
        whenever(mockState.gasUploadMap).thenReturn(mapOf(3 to CertificateUpload(10L, "old.pdf")))
        whenever(memberIdService.getParameterOrNull()).thenReturn(3)
        stubStateForAfterStepDataIsAdded()

        stepConfig.afterStepDataIsAdded(mockState)

        val updatedMapCaptor = argumentCaptor<Map<Int, CertificateUpload>>()
        verify(mockState).gasUploadMap = updatedMapCaptor.capture()
        assertEquals(CertificateUpload(55L, "updated.pdf"), updatedMapCaptor.firstValue[3])
        verify(mockState).highestAssignedGasMemberId = 3
    }

    private fun stubStateForAfterStepDataIsAdded() {
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.uploadGasCertStep).thenReturn(uploadGasCertStep)
        whenever(mockState.propertyOwnershipId).thenReturn(null)
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
        whenever(landlord.id).thenReturn(7L)
    }

    private fun setupStepConfig(): UploadGasCertStepConfig {
        val stepConfig =
            UploadGasCertStepConfig(virusScanCallbackService, fileUploadCookieService, memberIdService, userToLandlordService)
        stepConfig.urlPath = UploadGasCertStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
