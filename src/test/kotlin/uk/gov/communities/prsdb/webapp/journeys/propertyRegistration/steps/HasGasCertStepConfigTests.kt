package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class HasGasCertStepConfigTests {
    @Mock
    lateinit var mockJourneyState: GasSafetyDetailState

    val routeSegment = HasGasCertStep.ROUTE_SEGMENT

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
    fun `mode returns null when hasCert is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to null))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns HAS_CERTIFICATE when hasCert is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to "true"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasGasCertMode.HAS_CERTIFICATE, result)
    }

    @Test
    fun `mode returns NO_CERTIFICATE when hasCert is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to "false"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasGasCertMode.NO_CERTIFICATE, result)
    }

    private fun setupStepConfig(): HasGasCertStepConfig {
        val stepConfig = HasGasCertStepConfig()
        stepConfig.urlPath = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
