package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class CheckElectricalSafetyAnswersStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyState

    private fun setupStepConfig(): CheckElectricalSafetyAnswersStepConfig {
        val stepConfig = CheckElectricalSafetyAnswersStepConfig()
        stepConfig.routeSegment = CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    @Test
    fun `chooseTemplate returns checkElectricalSafetyAnswersForm`() {
        val stepConfig = setupStepConfig()
        val result = stepConfig.chooseTemplate(mockState)
        assertEquals("forms/checkElectricalSafetyAnswersForm", result)
    }

    @Test
    fun `mode returns COMPLETE when form data exists`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT)).thenReturn(mapOf("key" to "value"))

        val result = stepConfig.mode(mockState)

        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `mode returns null when no form data exists`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }
}
