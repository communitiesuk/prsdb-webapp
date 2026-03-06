package uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.JoinPropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class NoMatchingPropertiesStepConfigTests {
    private val mockState: JoinPropertyAddressSearchState = mock()
    private val mockLookupAddressStep: LookupAddressStep = mock()
    private val mockFindPropertyByPrnStep: FindPropertyByPrnStep = mock()

    private val routeSegment = NoMatchingPropertiesStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns COMPLETE when form model is present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(emptyMap())

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `getStepSpecificContent returns URLs with journeyId`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val journeyId = "test-journey-id"
        whenever(mockState.lookupAddressStep).thenReturn(mockLookupAddressStep)
        whenever(mockLookupAddressStep.currentJourneyId).thenReturn(journeyId)
        whenever(mockLookupAddressStep.isStepReachable).thenReturn(true)
        whenever(mockLookupAddressStep.routeSegment).thenReturn(LookupAddressStep.ROUTE_SEGMENT)
        whenever(mockState.findPropertyByPrnStep).thenReturn(mockFindPropertyByPrnStep)
        whenever(mockFindPropertyByPrnStep.currentJourneyId).thenReturn(journeyId)
        whenever(mockFindPropertyByPrnStep.isStepReachable).thenReturn(true)
        whenever(mockFindPropertyByPrnStep.routeSegment).thenReturn(FindPropertyByPrnStep.ROUTE_SEGMENT)

        // Act
        val result = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals("${LookupAddressStep.ROUTE_SEGMENT}?journeyId=$journeyId", result["searchAgainUrl"])
        assertEquals("${FindPropertyByPrnStep.ROUTE_SEGMENT}?journeyId=$journeyId", result["findByPrnUrl"])
    }

    @Test
    fun `getStepSpecificContent returns postcode from state when available`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.lookupAddressStep).thenReturn(mockLookupAddressStep)
        whenever(mockLookupAddressStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockLookupAddressStep.isStepReachable).thenReturn(true)
        whenever(mockLookupAddressStep.routeSegment).thenReturn(LookupAddressStep.ROUTE_SEGMENT)
        whenever(mockState.findPropertyByPrnStep).thenReturn(mockFindPropertyByPrnStep)
        whenever(mockFindPropertyByPrnStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockFindPropertyByPrnStep.isStepReachable).thenReturn(true)
        whenever(mockFindPropertyByPrnStep.routeSegment).thenReturn(FindPropertyByPrnStep.ROUTE_SEGMENT)
        whenever(mockState.getStepData(LookupAddressStep.ROUTE_SEGMENT))
            .thenReturn(mapOf("postcode" to "NW1 1AA", "houseNameOrNumber" to "42"))

        // Act
        val result = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals("NW1 1AA", result["postcode"])
        assertEquals("42", result["houseNameOrNumber"])
    }

    @Test
    fun `getStepSpecificContent returns placeholder values when state has no find property data`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.lookupAddressStep).thenReturn(mockLookupAddressStep)
        whenever(mockLookupAddressStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockLookupAddressStep.isStepReachable).thenReturn(true)
        whenever(mockLookupAddressStep.routeSegment).thenReturn(LookupAddressStep.ROUTE_SEGMENT)
        whenever(mockState.findPropertyByPrnStep).thenReturn(mockFindPropertyByPrnStep)
        whenever(mockFindPropertyByPrnStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockFindPropertyByPrnStep.isStepReachable).thenReturn(true)
        whenever(mockFindPropertyByPrnStep.routeSegment).thenReturn(FindPropertyByPrnStep.ROUTE_SEGMENT)
        whenever(mockState.getStepData(LookupAddressStep.ROUTE_SEGMENT)).thenReturn(null)

        // Act
        val result = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals("the postcode", result["postcode"])
        assertEquals("the house name or number", result["houseNameOrNumber"])
    }

    private fun setupStepConfig(): NoMatchingPropertiesStepConfig {
        val stepConfig = NoMatchingPropertiesStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
