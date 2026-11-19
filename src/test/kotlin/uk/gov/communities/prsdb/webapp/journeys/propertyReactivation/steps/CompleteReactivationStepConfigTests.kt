package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.services.PropertyReactivationService
import kotlin.test.assertIs

@ExtendWith(MockitoExtension::class)
class CompleteReactivationStepConfigTests {
    @Mock
    private lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    private lateinit var mockPropertyReactivationService: PropertyReactivationService

    @Mock
    private lateinit var mockState: PropertyReactivationJourneyState

    private lateinit var stepConfig: CompleteReactivationStepConfig

    private val propertyOwnershipId = 123L
    private val uprn = 456L
    private val defaultDestination = Destination.ExternalUrl("redirect")

    @BeforeEach
    fun setUp() {
        stepConfig =
            CompleteReactivationStepConfig(
                addressAvailabilityService = mockAddressAvailabilityService,
                propertyReactivationService = mockPropertyReactivationService,
            )
    }

    @Test
    fun `afterStepIsReached reactivates the property when its address has not been registered by anyone else`() {
        // Arrange
        whenever(mockState.uprn).thenReturn(uprn)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(false)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyReactivationService).reactivateProperty(propertyOwnershipId)
        verify(mockState, never()).isPropertyAlreadyRegistered = any()
    }

    @Test
    fun `afterStepIsReached marks the property as already registered and does not reactivate it when its address has been registered`() {
        // Arrange
        whenever(mockState.uprn).thenReturn(uprn)
        whenever(mockAddressAvailabilityService.isAddressOwned(uprn)).thenReturn(true)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).isPropertyAlreadyRegistered = true
        verifyNoInteractions(mockPropertyReactivationService)
    }

    @Test
    fun `afterStepIsReached reactivates the property without checking its address when it has no UPRN`() {
        // Arrange
        whenever(mockState.uprn).thenReturn(null)
        whenever(mockState.propertyOwnershipId).thenReturn(propertyOwnershipId)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyReactivationService).reactivateProperty(propertyOwnershipId)
        verifyNoInteractions(mockAddressAvailabilityService)
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
