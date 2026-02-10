package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FileCategory
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.services.CertificateUploadService

@ExtendWith(MockitoExtension::class)
class EicrUploadStepTests {
    @Mock
    lateinit var mockCertificateUploadService: CertificateUploadService

    @Mock
    lateinit var mockEicrState: EicrState

    @Mock
    lateinit var mockUploadStep: EicrUploadStep

    @Mock
    lateinit var mockStepConfig: EicrUploadStepConfig

    @Mock
    lateinit var mockSavedJourneyState: SavedJourneyState

    @Test
    fun `afterSaveState saves certificate upload when file upload ID exists`() {
        // Arrange
        val stepConfig = EicrUploadStepConfig(mockCertificateUploadService)
        val propertyId = 123L
        val fileUploadId = 456L

        whenever(mockEicrState.propertyId).thenReturn(propertyId)
        whenever(mockEicrState.getEicrCertificateFileUploadId()).thenReturn(fileUploadId)

        // Act
        stepConfig.afterSaveState(mockEicrState, mockSavedJourneyState)

        // Assert
        verify(mockCertificateUploadService).saveCertificateUpload(
            propertyId,
            fileUploadId,
            FileCategory.GasSafetyCert,
        )
    }

    @Test
    fun `afterSaveState on stepConfig is called by the step on saveStateIfAllowed`() {
        whenever(mockStepConfig.saveState(mockEicrState)).thenReturn(mockSavedJourneyState)

        // Arrange
        val eicrUploadStep = EicrUploadStep(mockStepConfig)
        eicrUploadStep.initialize(
            EicrUploadStep.ROUTE_SEGMENT,
            mockEicrState,
            mock(),
            mock(),
            mock(),
            mock(),
            true,
        )

        // Act
        eicrUploadStep.saveStateIfAllowed()

        // Assert
        verify(mockStepConfig).afterSaveState(mockEicrState, mockSavedJourneyState)
    }
}
