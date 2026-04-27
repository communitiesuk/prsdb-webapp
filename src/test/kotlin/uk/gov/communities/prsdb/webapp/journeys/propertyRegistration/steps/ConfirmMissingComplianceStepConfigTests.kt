package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ConfirmMissingComplianceStepConfigTests {
    @Mock
    lateinit var mockState: PropertyRegistrationJourneyState

    private val routeSegment = ConfirmMissingComplianceStep.ROUTE_SEGMENT

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
    fun `mode returns null when wantsToProceed is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("wantsToProceed" to null))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns CONFIRMED when wantsToProceed is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("wantsToProceed" to "true"))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(ConfirmMissingComplianceMode.CONFIRMED, result)
    }

    @Test
    fun `mode returns GO_BACK when wantsToProceed is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("wantsToProceed" to "false"))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(ConfirmMissingComplianceMode.GO_BACK, result)
    }

    @Test
    fun `chooseTemplate returns correct template path`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/confirmMissingCompliance", result)
    }

    private fun setupStepConfig(): ConfirmMissingComplianceStepConfig {
        val stepConfig = ConfirmMissingComplianceStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
