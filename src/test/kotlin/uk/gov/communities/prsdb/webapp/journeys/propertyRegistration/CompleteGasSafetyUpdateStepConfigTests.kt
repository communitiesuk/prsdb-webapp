package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.gasSafety.CompleteGasSafetyUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.gasSafety.UpdateGasSafetyJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.GasSafetyDetailsDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.UploadService

@ExtendWith(MockitoExtension::class)
class CompleteGasSafetyUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockState: UpdateGasSafetyJourneyState

    @Mock
    private lateinit var mockComplianceDetailsDataModelFactory: ComplianceDetailsDataModelFactory

    @Mock
    private lateinit var mockUploadService: UploadService

    private lateinit var stepConfig: CompleteGasSafetyUpdateStepConfig

    private val propertyId = 123L
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig =
            CompleteGasSafetyUpdateStepConfig(mockPropertyComplianceService, mockComplianceDetailsDataModelFactory, mockUploadService)
    }

    @Nested
    inner class AfterStepIsReached {
        @Mock
        private lateinit var mockDetailTask: GasSafetyDetailsTask

        private val gasSafety =
            GasSafetyDetailsDataModel(hasGasSupply = true, issueDate = LocalDate(2025, 6, 15).toJavaLocalDate(), uploadIds = listOf(1L, 2L))

        @BeforeEach
        fun setUp() {
            whenever(mockState.propertyId).thenReturn(propertyId)
            whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
            whenever(mockState.gasSafetyDetailsTask).thenReturn(mockDetailTask)
            whenever(mockComplianceDetailsDataModelFactory.fromGasSafetyDetailState(mockDetailTask)).thenReturn(gasSafety)
        }

        @Test
        fun `calls updateGasSafety with the gas safety details from the journey`() {
            whenever(mockState.previousUploadIds).thenReturn(emptyList())

            stepConfig.afterStepIsReached(mockState)

            verify(mockPropertyComplianceService).updateGasSafety(
                propertyOwnershipId = propertyId,
                initialLastModifiedDate = initialLastModifiedDate,
                hasGasSupply = gasSafety.hasGasSupply,
                gasSafetyCertIssueDate = gasSafety.issueDate,
                gasSafetyCertUploadIds = gasSafety.uploadIds,
            )
        }

        @Test
        fun `deletes the journey then rethrows when it gets an UpdateConflictException`() {
            // Arrange
            whenever(
                mockPropertyComplianceService.updateGasSafety(
                    propertyOwnershipId = propertyId,
                    initialLastModifiedDate = initialLastModifiedDate,
                    hasGasSupply = gasSafety.hasGasSupply,
                    gasSafetyCertIssueDate = gasSafety.issueDate,
                    gasSafetyCertUploadIds = gasSafety.uploadIds,
                ),
            ).thenThrow(UpdateConflictException::class.java)

            // Act, assert
            assertThrows<UpdateConflictException> { stepConfig.afterStepIsReached(mockState) }

            verify(mockState).deleteJourney()
        }

        @Test
        fun `deletes each previous file upload`() {
            whenever(mockState.previousUploadIds).thenReturn(mutableListOf(10L, 20L))

            stepConfig.afterStepIsReached(mockState)

            verify(mockUploadService).deleteUploadedFile(10L)
            verify(mockUploadService).deleteUploadedFile(20L)
        }
    }

    @Test
    fun `resolveNextDestination calls deleteJourney on state and returns the default destination`() {
        val defaultDestination = Destination.ExternalUrl("redirect")

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        verify(mockState).deleteJourney()
        assert(result == defaultDestination)
    }
}
