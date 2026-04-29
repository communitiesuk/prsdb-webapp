package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.gasSafety.CompleteGasSafetyUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.gasSafety.UpdateGasSafetyJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSupplyFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService

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

    private lateinit var stepConfig: CompleteGasSafetyUpdateStepConfig

    private val propertyId = 123L
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig = CompleteGasSafetyUpdateStepConfig(mockPropertyComplianceService)
    }

    @Test
    fun `afterStepIsReached calls updateGasSafety with gas supply, issue date and upload ids`() {
        val issueDate = LocalDate(2025, 6, 15)
        val uploadIds = listOf(1L, 2L)

        whenever(mockState.propertyId).thenReturn(propertyId)
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
    fun `afterStepIsReached calls updateGasSafety with no gas supply and null issue date`() {
        whenever(mockState.propertyId).thenReturn(propertyId)
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
    fun `afterStepIsReached throws NotNullFormModelValueIsNullException when hasGasSupply is null`() {
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
        whenever(mockHasGasSupplyStep.formModel).thenReturn(mockGasSupplyFormModel)
        whenever(mockGasSupplyFormModel.hasGasSupply).thenReturn(null)

        assertThrows<NotNullFormModelValueIsNullException> {
            stepConfig.afterStepIsReached(mockState)
        }
    }

    @Test
    fun `afterStepIsReached deletes the journey then rethrows when it gets an UpdateConflictException`() {
        // Arrange
        whenever(mockState.propertyId).thenReturn(propertyId)
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
    fun `resolveNextDestination calls deleteJourney on state and returns the default destination`() {
        val defaultDestination = Destination.ExternalUrl("redirect")

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        verify(mockState).deleteJourney()
        assert(result == defaultDestination)
    }
}
