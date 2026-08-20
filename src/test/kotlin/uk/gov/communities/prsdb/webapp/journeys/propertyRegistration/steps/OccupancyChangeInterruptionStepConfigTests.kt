package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.WhoProvidesDetailsTask
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

    @Test
    fun `afterStepDataIsAdded clears the delegation answers and redirects to the task list`() {
        val whoProvidesTask = mock<WhoProvidesDetailsTask>()
        val state =
            mock<PropertyRegistrationJourneyState> {
                on { whoProvidesDetailsTask } doReturn whoProvidesTask
                on { baseJourneyId } doReturn "base-journey-id"
            }

        setupStepConfig().afterStepDataIsAdded(state)

        verify(whoProvidesTask).cachedWhoProvidesRentalDetails = null
        verify(state).clearStepData(WhoProvidesRentalDetailsStep.ROUTE_SEGMENT)
        verify(state).clearStepData(LettingAgentEmailStep.ROUTE_SEGMENT)
        argumentCaptor<Destination>().apply {
            verify(state).returnToCyaPageDestination = capture()
            val destination = firstValue as Destination.StepRoute
            assertEquals(TASK_LIST_PATH_SEGMENT, destination.routeSegment)
            assertEquals("base-journey-id", destination.journeyId)
        }
    }

    private fun setupStepConfig(): OccupancyChangeInterruptionStepConfig {
        val stepConfig = OccupancyChangeInterruptionStepConfig()
        stepConfig.urlPath = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
