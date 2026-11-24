package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.propertyManager

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.PropertyManager
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerDetailsStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyManagerTask
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class StartPropertyManagerUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockState: UpdatePropertyManagerJourneyState

    @Mock
    private lateinit var mockPropertyManagerTask: PropertyManagerTask

    @Mock
    private lateinit var mockPropertyManagerStep: PropertyManagerStep

    @Mock
    private lateinit var mockPropertyManagerDetailsStep: PropertyManagerDetailsStep

    private val propertyId = 123L

    private lateinit var stepConfig: StartPropertyManagerUpdateStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig = StartPropertyManagerUpdateStepConfig(mockPropertyOwnershipService)
    }

    @Test
    fun `afterStepIsReached prefills the property manager task and stores the original property manager`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        propertyOwnership.propertyManager = PropertyManager("Agent Ltd", "agent@example.com")
        whenever(mockState.propertyManagerInitialised).thenReturn(null)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockState.propertyManagerTask).thenReturn(mockPropertyManagerTask)
        whenever(mockPropertyManagerTask.propertyManagerStep).thenReturn(mockPropertyManagerStep)
        whenever(mockPropertyManagerTask.propertyManagerDetailsStep).thenReturn(mockPropertyManagerDetailsStep)
        whenever(mockPropertyManagerStep.stepConfig)
            .thenReturn(PropertyManagerStepConfig().apply { urlPath = PropertyManagerStep.ROUTE_SEGMENT })
        whenever(mockPropertyManagerDetailsStep.stepConfig)
            .thenReturn(PropertyManagerDetailsStepConfig().apply { urlPath = PropertyManagerDetailsStep.ROUTE_SEGMENT })

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).originalPropertyManagerName = "Agent Ltd"
        verify(mockState).originalPropertyManagerEmail = "agent@example.com"
        verify(mockState).addStepData(
            PropertyManagerStep.ROUTE_SEGMENT,
            PropertyManagerFormModel().apply { usingPropertyManager = true }.toPageData(),
        )
        verify(mockState).addStepData(
            PropertyManagerDetailsStep.ROUTE_SEGMENT,
            PropertyManagerDetailsFormModel()
                .apply {
                    name = "Agent Ltd"
                    emailAddress = "agent@example.com"
                }.toPageData(),
        )
        verify(mockState).propertyManagerInitialised = true
    }

    @Test
    fun `afterStepIsReached only marks the state as initialised when the property has no property manager`() {
        whenever(mockState.propertyManagerInitialised).thenReturn(null)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId))
            .thenReturn(MockLandlordData.createPropertyOwnership(id = propertyId))

        stepConfig.afterStepIsReached(mockState)

        verify(mockState, never()).addStepData(any(), any())
        verify(mockState, never()).originalPropertyManagerName = any()
        verify(mockState).propertyManagerInitialised = true
    }

    @Test
    fun `afterStepIsReached does not re-initialise when already initialised`() {
        whenever(mockState.propertyManagerInitialised).thenReturn(true)

        stepConfig.afterStepIsReached(mockState)

        verifyNoInteractions(mockPropertyOwnershipService)
        verify(mockState, never()).addStepData(any(), any())
    }

    @Test
    fun `mode is complete once the property manager has been initialised`() {
        whenever(mockState.propertyManagerInitialised).thenReturn(true)

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `mode is null before the property manager has been initialised`() {
        whenever(mockState.propertyManagerInitialised).thenReturn(null)

        assertNull(stepConfig.mode(mockState))
    }
}
