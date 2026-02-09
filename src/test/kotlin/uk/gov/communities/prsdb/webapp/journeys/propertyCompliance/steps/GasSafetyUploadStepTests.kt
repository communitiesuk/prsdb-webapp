package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.constants.enums.FileCategory
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.services.CertificateUploadService

@ExtendWith(MockitoExtension::class)
class GasSafetyUploadStepTests {
    @Mock
    lateinit var mockCertificateUploadService: CertificateUploadService

    @Mock
    lateinit var mockGasSafetyState: GasSafetyState

    @Mock
    lateinit var mockUploadStep: GasSafetyCertificateUploadStep

    @Mock
    lateinit var mockStepConfig: GasSafetyCertificateUploadStepConfig

    @Mock
    lateinit var mockSavedJourneyState: SavedJourneyState

    @Test
    fun `afterSaveState saves certificate upload when file upload ID exists`() {
        // Arrange
        val stepConfig = GasSafetyCertificateUploadStepConfig(mockCertificateUploadService)
        val propertyId = 123L
        val fileUploadId = 456L

        whenever(mockGasSafetyState.propertyId).thenReturn(propertyId)
        whenever(mockGasSafetyState.getGasSafetyCertificateFileUploadId()).thenReturn(fileUploadId)

        // Act
        stepConfig.afterSaveState(mockGasSafetyState, mockSavedJourneyState)

        // Assert
        verify(mockCertificateUploadService).saveCertificateUpload(
            propertyId,
            fileUploadId,
            FileCategory.GasSafetyCert,
        )
    }

    @Test
    fun `afterSaveState on stepConfig is called by the step on saveStateIfAllowed`() {
        whenever(mockStepConfig.saveState(mockGasSafetyState)).thenReturn(mockSavedJourneyState)

        // Arrange
        val gasSafetyUploadStep = GasSafetyCertificateUploadStep(mockStepConfig)
        gasSafetyUploadStep.initialize(
            GasSafetyCertificateUploadStep.ROUTE_SEGMENT,
            mockGasSafetyState,
            Mockito.mock(),
            Mockito.mock(),
            Mockito.mock(),
            Mockito.mock(),
            true,
        )

        // Act
        gasSafetyUploadStep.saveStateIfAllowed()

        // Assert
        verify(mockStepConfig).afterSaveState(mockGasSafetyState, mockSavedJourneyState)
    }

    @Test
    fun `saveStateIfAllowed on the step is called by stepLifecycleOrchestrator on postStepModelAndView`() {
        // Arrange
        val stepLifecycleOrchestrator = StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator(mockUploadStep)
        whenever(mockUploadStep.attemptToReachStep()).thenReturn(true)

        val bindingResult = Mockito.mock<BindingResult>()
        whenever(bindingResult.hasErrors()).thenReturn(false)
        whenever(mockUploadStep.validateSubmittedData(anyOrNull())).thenReturn(bindingResult)

        val redirectUrl = "redirectUrl"
        whenever(mockUploadStep.getNextDestination()).thenReturn(Destination.ExternalUrl(redirectUrl))

        // Act
        stepLifecycleOrchestrator.postStepModelAndView(mapOf())

        // Assert
        verify(mockUploadStep).saveStateIfAllowed()
    }
}
