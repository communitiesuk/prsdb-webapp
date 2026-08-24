package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.OccupancyLettingAgentInterruptionStepConfig

class OccupancyLettingAgentInterruptionStepConfigTests {
    private val stepConfig = OccupancyLettingAgentInterruptionStepConfig()

    @Test
    fun `chooseTemplate returns the letting agent interruption template`() {
        assertEquals("forms/occupancyLettingAgentInterruptionForm", stepConfig.chooseTemplate(mock()))
    }
}
