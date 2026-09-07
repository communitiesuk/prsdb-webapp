package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

class OccupancyChangeInterruptionStepConfigTests {
    private val routeSegment = OccupancyChangeInterruptionStep.ROUTE_SEGMENT

    @Test
    fun `mode returns COMPLETE when the interruption form is submitted`() {
        val config = setupStepConfig()
        val state = mock<PropertyRegistrationJourneyState> { on { getStepData(routeSegment) } doReturn emptyMap() }

        assertEquals(Complete.COMPLETE, config.mode(state))
    }

    @Test
    fun `mode returns null when the interruption form has not been submitted`() {
        val config = setupStepConfig()
        val state = mock<PropertyRegistrationJourneyState> { on { getStepData(routeSegment) } doReturn null }

        assertNull(config.mode(state))
    }

    @Test
    fun `chooseTemplate returns the occupancy change interruption form`() {
        val config = setupStepConfig()

        assertEquals("forms/occupancyChangeInterruptionForm", config.chooseTemplate(mock<PropertyRegistrationJourneyState>()))
    }

    private fun setupStepConfig(): OccupancyChangeInterruptionStepConfig {
        val stepConfig = OccupancyChangeInterruptionStepConfig()
        stepConfig.urlPath = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
