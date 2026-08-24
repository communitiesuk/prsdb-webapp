package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState

class LettingAgentInterruptionStateTests {
    @Test
    fun `showsLettingAgentInterruption is true when the property was occupied and is delegated to a letting agent`() {
        assertTrue(buildTestState(wasOccupied = true, isDelegated = true).showsLettingAgentInterruption)
    }

    @Test
    fun `showsLettingAgentInterruption is false when the property was not occupied`() {
        assertFalse(buildTestState(wasOccupied = false, isDelegated = true).showsLettingAgentInterruption)
    }

    @Test
    fun `showsLettingAgentInterruption is false when the property is not delegated to a letting agent`() {
        assertFalse(buildTestState(wasOccupied = true, isDelegated = false).showsLettingAgentInterruption)
    }

    @Test
    fun `showsLettingAgentInterruption is false when the property was not occupied and is not delegated`() {
        assertFalse(buildTestState(wasOccupied = false, isDelegated = false).showsLettingAgentInterruption)
    }

    private fun buildTestState(
        wasOccupied: Boolean,
        isDelegated: Boolean,
    ): LettingAgentInterruptionState =
        object : AbstractJourneyState(journeyStateService = mock()), LettingAgentInterruptionState {
            override val wasOccupied = wasOccupied
            override val isDelegatedToLettingAgent = isDelegated
        }
}
