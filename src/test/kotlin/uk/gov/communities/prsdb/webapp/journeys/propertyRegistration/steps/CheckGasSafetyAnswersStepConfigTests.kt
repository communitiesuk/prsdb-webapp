package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class CheckGasSafetyAnswersStepConfigTests {
    @Mock
    lateinit var mockState: GasSafetyState

    private fun setupStepConfig(): CheckGasSafetyAnswersStepConfig {
        val stepConfig = CheckGasSafetyAnswersStepConfig()
        stepConfig.routeSegment = CheckGasSafetyAnswersStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    @Test
    fun `chooseTemplate returns checkGasSafetyAnswersForm`() {
        val stepConfig = setupStepConfig()
        val result = stepConfig.chooseTemplate(mockState)
        assertEquals("forms/checkGasSafetyAnswersForm", result)
    }

    @Test
    fun `mode returns COMPLETE when form data exists`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(CheckGasSafetyAnswersStep.ROUTE_SEGMENT)).thenReturn(mapOf("key" to "value"))

        val result = stepConfig.mode(mockState)

        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `mode returns null when no form data exists`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(CheckGasSafetyAnswersStep.ROUTE_SEGMENT)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }
}
