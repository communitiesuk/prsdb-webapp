package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class AreYouSureStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockState: PropertyDeregistrationJourneyState

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns null when wantsToProceed is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to null))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns WANTS_TO_PROCEED when wantsToProceed is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to "true"))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(AreYouSureMode.WANTS_TO_PROCEED, result)
    }

    @Test
    fun `mode returns DOES_NOT_WANT_TO_PROCEED when wantsToProceed is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to "false"))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(AreYouSureMode.DOES_NOT_WANT_TO_PROCEED, result)
    }

    private fun setupStepConfig(): AreYouSureStepConfig {
        val stepConfig = AreYouSureStepConfig(mockPropertyOwnershipService)
        stepConfig.routeSegment = AreYouSureStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
