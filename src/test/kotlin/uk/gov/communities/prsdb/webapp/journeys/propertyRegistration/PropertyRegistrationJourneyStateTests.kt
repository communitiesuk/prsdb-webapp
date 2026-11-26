package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PropertyRegistrationJourneyStateTests {
    private val state: PropertyRegistrationJourneyState = mock(defaultAnswer = Answers.CALLS_REAL_METHODS)

    @Test
    fun `isMissingComplianceCheckApplicable is true when occupied and not delegated to a letting agent`() {
        doReturn(true).whenever(state).isOccupied
        doReturn(false).whenever(state).isDelegatedToLettingAgent()

        assertTrue(state.isMissingComplianceCheckApplicable)
    }

    @Test
    fun `isMissingComplianceCheckApplicable is false when delegated to a letting agent`() {
        doReturn(true).whenever(state).isOccupied
        doReturn(true).whenever(state).isDelegatedToLettingAgent()

        assertFalse(state.isMissingComplianceCheckApplicable)
    }

    @Test
    fun `isMissingComplianceCheckApplicable is false when not occupied`() {
        doReturn(false).whenever(state).isOccupied

        assertFalse(state.isMissingComplianceCheckApplicable)
    }
}
