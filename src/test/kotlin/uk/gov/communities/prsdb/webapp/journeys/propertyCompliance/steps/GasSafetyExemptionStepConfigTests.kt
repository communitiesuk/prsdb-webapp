package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.ExemptionMode
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class GasSafetyExemptionStepConfigTests {
    @Mock
    lateinit var mockJourneyState: JourneyState

    val routeSegment = GasSafetyExemptionStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns null when hasExemption is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(emptyMap())

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns HAS_EXEMPTION when hasExemption is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("hasExemption" to true))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(ExemptionMode.HAS_EXEMPTION, result)
    }

    @Test
    fun `mode returns NO_EXEMPTION when hasExemption is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("hasExemption" to false))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(ExemptionMode.NO_EXEMPTION, result)
    }

    private fun setupStepConfig(): GasSafetyExemptionStepConfig {
        val stepConfig = GasSafetyExemptionStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
