package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

class OccupancyLettingAgentInterruptionStepConfigTests {
    private val routeSegment = OccupancyLettingAgentInterruptionStep.ROUTE_SEGMENT

    @Test
    fun `mode returns COMPLETE when the interruption form is submitted`() {
        val config = setupStepConfig()
        val state = mock<UpdateOccupancyJourneyState> { on { getStepData(routeSegment) } doReturn emptyMap() }

        assertEquals(Complete.COMPLETE, config.mode(state))
    }

    @Test
    fun `mode returns null when the interruption form has not been submitted`() {
        val config = setupStepConfig()
        val state = mock<UpdateOccupancyJourneyState> { on { getStepData(routeSegment) } doReturn null }

        assertNull(config.mode(state))
    }

    @Test
    fun `chooseTemplate returns the letting agent interruption template`() {
        val config = setupStepConfig()

        assertEquals("forms/occupancyLettingAgentInterruptionForm", config.chooseTemplate(mock<UpdateOccupancyJourneyState>()))
    }

    private fun setupStepConfig(): OccupancyLettingAgentInterruptionStepConfig {
        val stepConfig = OccupancyLettingAgentInterruptionStepConfig()
        stepConfig.urlPath = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
