package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ProvideTenancyDetailsLaterStepConfigTests {
    @Mock
    lateinit var mockState: HouseholdsAndTenantsState

    @Test
    fun `GetStepSpecificContent provides save and continue submitButtonText`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals("forms.buttons.saveAndContinue", content["submitButtonText"])
    }

    @Test
    fun `ChooseTemplate returns expected template`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act & Assert
        assertEquals("forms/provideTenancyDetailsLaterForm", stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `Mode returns COMPLETE when form model is saved`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act
        whenever(mockState.getStepData(ProvideTenancyDetailsLaterStep.ROUTE_SEGMENT)).thenReturn(emptyMap<String, Any>())

        // Assert
        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    @Test
    fun `Mode returns null when form model is not saved`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(ProvideTenancyDetailsLaterStep.ROUTE_SEGMENT)).thenReturn(null)

        // Act & Assert
        assertNull(stepConfig.mode(mockState))
    }

    private fun setupStepConfig(): ProvideTenancyDetailsLaterStepConfig {
        val stepConfig = ProvideTenancyDetailsLaterStepConfig()
        stepConfig.routeSegment = ProvideTenancyDetailsLaterStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
