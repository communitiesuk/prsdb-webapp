package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import jakarta.persistence.EntityExistsException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyReactivationDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyReactivationService
import kotlin.test.assertIs

@ExtendWith(MockitoExtension::class)
class CompleteReactivationStepConfigTests {
    @Mock
    private lateinit var mockPropertyReactivationDataModelFactory: PropertyReactivationDataModelFactory

    @Mock
    private lateinit var mockPropertyReactivationService: PropertyReactivationService

    @Mock
    private lateinit var mockState: PropertyReactivationJourneyState

    private lateinit var stepConfig: CompleteReactivationStepConfig

    private val propertyOwnershipId = 123L
    private val defaultDestination = Destination.ExternalUrl("redirect")
    private val reactivationData = PropertyReactivationDataModel()

    @BeforeEach
    fun setUp() {
        stepConfig =
            CompleteReactivationStepConfig(
                propertyReactivationDataModelFactory = mockPropertyReactivationDataModelFactory,
                propertyReactivationService = mockPropertyReactivationService,
            )
    }

    @Test
    fun `afterStepIsReached reactivates the property without payment`() {
        // Arrange
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyReactivationDataModelFactory.fromJourneyState(mockState)).thenReturn(reactivationData)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyReactivationService).reactivatePropertyWithoutPayment(propertyOwnershipId, reactivationData)
        verify(mockState, never()).isPropertyAlreadyRegistered = any()
    }

    @Test
    fun `afterStepIsReached marks the property as already registered when reactivation throws an EntityExistsException`() {
        // Arrange
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockPropertyReactivationDataModelFactory.fromJourneyState(mockState)).thenReturn(reactivationData)
        whenever(mockPropertyReactivationService.reactivatePropertyWithoutPayment(propertyOwnershipId, reactivationData))
            .thenThrow(EntityExistsException("Address already registered"))

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).isPropertyAlreadyRegistered = true
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination when the property was reactivated`() {
        // Arrange
        whenever(mockState.isPropertyAlreadyRegistered).thenReturn(false)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        verify(mockState).deleteJourney()
        assertEquals(defaultDestination, result)
    }

    @Test
    fun `resolveNextDestination keeps the journey and goes to the already registered step when the address has been registered`() {
        // Arrange
        val mockAlreadyRegisteredStep = mock<ReactivationAlreadyRegisteredStep>()
        whenever(mockAlreadyRegisteredStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockState.isPropertyAlreadyRegistered).thenReturn(true)
        whenever(mockState.alreadyRegisteredStep).thenReturn(mockAlreadyRegisteredStep)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        verify(mockState, never()).deleteJourney()
        assertEquals(mockAlreadyRegisteredStep, assertIs<Destination.VisitableStep>(result).step)
    }
}
