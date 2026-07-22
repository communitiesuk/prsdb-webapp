package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ProvideTenancyDetailsLaterStepConfigTests {
    @Mock
    private lateinit var mockState: HouseholdsAndTenantsState

    @Test
    fun `GetStepSpecificContent provides save and continue submitButtonText and chooseTemplate returns expected template`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals("forms.buttons.saveAndContinue", content["submitButtonText"])
        assertEquals("forms/provideTenancyDetailsLaterForm", stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `Mode returns COMPLETE when form model is saved`() {
        // Arrange
        val stepConfig = ProvideTenancyDetailsLaterStepConfig()
        stepConfig.routeSegment = ProvideTenancyDetailsLaterStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        // Act
        whenever(mockState.getStepData(ProvideTenancyDetailsLaterStep.ROUTE_SEGMENT)).thenReturn(emptyMap<String, Any>())

        // Assert
        assertEquals(Complete.COMPLETE, stepConfig.mode(mockState))
    }

    private fun setupStepConfig(): ProvideTenancyDetailsLaterStepConfig {
        val stepConfig = ProvideTenancyDetailsLaterStepConfig()
        stepConfig.routeSegment = ProvideTenancyDetailsLaterStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
