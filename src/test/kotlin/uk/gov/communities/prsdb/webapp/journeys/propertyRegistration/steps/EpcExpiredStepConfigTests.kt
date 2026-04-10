package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class EpcExpiredStepConfigTests {
    @Mock
    lateinit var mockState: EpcState

    @Test
    fun `chooseTemplate returns occupied template when isOccupied is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(true)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/epcExpiredForOccupiedPropertyRegistration", result)
    }

    @Test
    fun `chooseTemplate returns unoccupied template when isOccupied is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(false)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/epcExpiredForUnoccupiedPropertyRegistration", result)
    }

    @Test
    fun `chooseTemplate throws IllegalStateException when isOccupied is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(null)

        // Act & Assert
        assertThrows<IllegalStateException> { stepConfig.chooseTemplate(mockState) }
    }

    private fun setupStepConfig(): EpcExpiredStepConfig {
        val stepConfig = EpcExpiredStepConfig()
        stepConfig.routeSegment = EpcExpiredStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
