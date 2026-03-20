package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ProvideElectricalCertLaterStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyState

    @Test
    fun `chooseTemplate returns occupied template when isOccupied is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(true)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/provideElectricalSafetyCertificateLaterForOccupiedProperty", result)
    }

    @Test
    fun `chooseTemplate returns unoccupied template when isOccupied is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(false)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/provideElectricalSafetyCertificateLaterForUnoccupiedProperty", result)
    }

    @Test
    fun `chooseTemplate throws IllegalStateException when isOccupied is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(null)

        // Act & Assert
        assertThrows<IllegalStateException> { stepConfig.chooseTemplate(mockState) }
    }

    private fun setupStepConfig(): ProvideElectricalCertLaterStepConfig {
        val stepConfig = ProvideElectricalCertLaterStepConfig()
        stepConfig.routeSegment = ProvideElectricalCertLaterStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
