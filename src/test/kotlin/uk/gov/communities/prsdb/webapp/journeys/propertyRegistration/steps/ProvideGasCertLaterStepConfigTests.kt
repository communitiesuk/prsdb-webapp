package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ProvideGasCertLaterStepConfigTests {
    @Mock
    lateinit var mockState: GasSafetyState

    @Test
    fun `chooseTemplate returns occupied template when isOccupied is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(true)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/provideGasCertificateLaterForOccupiedProperty", result)
    }

    @Test
    fun `chooseTemplate returns unoccupied template when isOccupied is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(false)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/provideGasCertificateLaterForUnoccupiedProperty", result)
    }

    private fun setupStepConfig(): ProvideGasCertLaterStepConfig {
        val stepConfig = ProvideGasCertLaterStepConfig()
        stepConfig.routeSegment = ProvideGasCertLaterStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
