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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.gasSafety.CompleteGasSafetyUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.gasSafety.UpdateGasSafetyJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSupplyFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.UploadService

@ExtendWith(MockitoExtension::class)
class CompleteGasSafetyUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockState: UpdateGasSafetyJourneyState

    @Mock
    private lateinit var mockHasGasSupplyStep: HasGasSupplyStep

    @Mock
    private lateinit var mockGasSupplyFormModel: GasSupplyFormModel

    @Mock
    private lateinit var mockUploadService: UploadService

    @Mock
    private lateinit var mockCompliance: PropertyCompliance

    private lateinit var stepConfig: CompleteGasSafetyUpdateStepConfig

    private val propertyId = 123L
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig = CompleteGasSafetyUpdateStepConfig(mockPropertyComplianceService, mockUploadService)
    }

    @Nested
    inner class AfterStepIsReached {
        @BeforeEach
        fun setUp() {
            whenever(mockState.propertyId).thenReturn(propertyId)
            whenever(mockPropertyComplianceService.getComplianceForProperty(propertyId)).thenReturn(mockCompliance)
            whenever(mockCompliance.electricalSafetyFileUploads).thenReturn(mutableListOf())
        }

        @Test
        fun `calls updateGasSafety with gas supply, issue date and upload ids`() {
            val issueDate = LocalDate(2025, 6, 15)
            val uploadIds = listOf(1L, 2L)

            whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockHasGasSupplyStep.formModel).thenReturn(mockGasSupplyFormModel)
            whenever(mockGasSupplyFormModel.hasGasSupply).thenReturn(true)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(issueDate)
            whenever(mockState.gasUploadIds).thenReturn(uploadIds)

            stepConfig.afterStepIsReached(mockState)

            verify(mockPropertyComplianceService).updateGasSafety(
                propertyOwnershipId = propertyId,
                initialLastModifiedDate = initialLastModifiedDate,
                hasGasSupply = true,
                gasSafetyCertIssueDate = issueDate.toJavaLocalDate(),
                gasSafetyCertUploadIds = uploadIds,
            )
        }

        @Test
        fun `calls updateGasSafety with no gas supply and null issue date`() {
            whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockHasGasSupplyStep.formModel).thenReturn(mockGasSupplyFormModel)
            whenever(mockGasSupplyFormModel.hasGasSupply).thenReturn(false)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(null)
            whenever(mockState.gasUploadIds).thenReturn(emptyList())

            stepConfig.afterStepIsReached(mockState)

            verify(mockPropertyComplianceService).updateGasSafety(
                propertyOwnershipId = propertyId,
                initialLastModifiedDate = initialLastModifiedDate,
                hasGasSupply = false,
                gasSafetyCertIssueDate = null,
                gasSafetyCertUploadIds = emptyList(),
            )
        }

        @Test
        fun `throws NotNullFormModelValueIsNullException when hasGasSupply is null`() {
            whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockHasGasSupplyStep.formModel).thenReturn(mockGasSupplyFormModel)
            whenever(mockGasSupplyFormModel.hasGasSupply).thenReturn(null)

            assertThrows<NotNullFormModelValueIsNullException> {
                stepConfig.afterStepIsReached(mockState)
            }
        }

        @Test
        fun `deletes the journey then rethrows when it gets an UpdateConflictException`() {
            // Arrange
            whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockHasGasSupplyStep.formModel).thenReturn(mockGasSupplyFormModel)
            whenever(mockGasSupplyFormModel.hasGasSupply).thenReturn(false)

            whenever(
                mockPropertyComplianceService.updateGasSafety(
                    propertyOwnershipId = propertyId,
                    initialLastModifiedDate = initialLastModifiedDate,
                    hasGasSupply = false,
                    gasSafetyCertIssueDate = null,
                    gasSafetyCertUploadIds = emptyList(),
                ),
            ).thenThrow(UpdateConflictException::class.java)

            // Act, assert
            assertThrows<UpdateConflictException> { stepConfig.afterStepIsReached(mockState) }

            verify(mockState).deleteJourney()
        }

        @Test
        fun `deletes each previous file upload`() {
            val mockFileUpload1 = mock<FileUpload>()
            val mockFileUpload2 = mock<FileUpload>()
            whenever(mockFileUpload1.id).thenReturn(10L)
            whenever(mockFileUpload2.id).thenReturn(20L)
            whenever(mockCompliance.electricalSafetyFileUploads).thenReturn(mutableListOf(mockFileUpload1, mockFileUpload2))

            whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockHasGasSupplyStep.formModel).thenReturn(mockGasSupplyFormModel)
            whenever(mockGasSupplyFormModel.hasGasSupply).thenReturn(false)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(null)
            whenever(mockState.gasUploadIds).thenReturn(emptyList())

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
