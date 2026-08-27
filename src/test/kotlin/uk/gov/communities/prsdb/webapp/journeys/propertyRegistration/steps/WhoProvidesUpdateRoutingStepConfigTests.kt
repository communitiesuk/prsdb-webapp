package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.WhoProvidesDetailsTask

class WhoProvidesUpdateRoutingStepConfigTests {
    @Nested
    inner class ModeTests {
        @Test
        fun `mode returns UNCHANGED when previously delegated and new answer is letting agent`() {
            val result = configuredConfig(previouslyDelegated = true).mode(stateWith(WhoProvidesRentalDetailsMode.LETTING_AGENT_PROVIDES))

            assertEquals(WhoProvidesUpdateRouteMode.UNCHANGED, result)
        }

        @Test
        fun `mode returns UNCHANGED when not previously delegated and new answer is landlord`() {
            val result = configuredConfig(previouslyDelegated = false).mode(stateWith(WhoProvidesRentalDetailsMode.LANDLORD_PROVIDES))

            assertEquals(WhoProvidesUpdateRouteMode.UNCHANGED, result)
        }

        @Test
        fun `mode returns CHANGED_TO_LETTING_AGENT when not previously delegated and new answer is letting agent`() {
            val result =
                configuredConfig(previouslyDelegated = false).mode(stateWith(WhoProvidesRentalDetailsMode.LETTING_AGENT_PROVIDES))

            assertEquals(WhoProvidesUpdateRouteMode.CHANGED_TO_LETTING_AGENT, result)
        }

        @Test
        fun `mode returns CHANGED_TO_LANDLORD when previously delegated and new answer is landlord`() {
            val result = configuredConfig(previouslyDelegated = true).mode(stateWith(WhoProvidesRentalDetailsMode.LANDLORD_PROVIDES))

            assertEquals(WhoProvidesUpdateRouteMode.CHANGED_TO_LANDLORD, result)
        }

        @Test
        fun `mode returns null without reading the previous answer when new answer is null`() {
            var previouslyDelegatedWasRead = false
            val config =
                WhoProvidesUpdateRoutingStepConfig().apply {
                    usingPreviouslyDelegated {
                        previouslyDelegatedWasRead = true
                        true
                    }
                }

            val result = config.mode(stateWith(null))

            assertNull(result)
            assertFalse(previouslyDelegatedWasRead)
        }
    }

    @Nested
    inner class PreviouslyDelegatedTests {
        @Test
        fun `base journey lookup returns true when the previous answer is letting agent`() {
            val result = getPreviouslyDelegatedFromBaseJourney(WhoProvidesRentalDetails.LETTING_AGENT)

            assertTrue(result)
        }

        @Test
        fun `base journey lookup returns false when the previous answer is landlord`() {
            val result = getPreviouslyDelegatedFromBaseJourney(WhoProvidesRentalDetails.LANDLORD)

            assertFalse(result)
        }

        @Test
        fun `base journey lookup returns false when there is no previous answer`() {
            val result = getPreviouslyDelegatedFromBaseJourney(null)

            assertFalse(result)
        }
    }

    private fun configuredConfig(previouslyDelegated: Boolean) =
        WhoProvidesUpdateRoutingStepConfig().apply {
            usingPreviouslyDelegated { previouslyDelegated }
        }

    private fun stateWith(newMode: WhoProvidesRentalDetailsMode?): PropertyRegistrationJourneyState {
        val step = mock<WhoProvidesRentalDetailsStep> { on { outcome } doReturn newMode }
        val task = mock<WhoProvidesDetailsTask> { on { whoProvidesRentalDetailsStep } doReturn step }
        return mock { on { whoProvidesDetailsTask } doReturn task }
    }

    private fun getPreviouslyDelegatedFromBaseJourney(previousAnswer: WhoProvidesRentalDetails?): Boolean {
        val baseState =
            mock<PropertyRegistrationJourneyState> {
                on { cachedWhoProvidesRentalDetails } doReturn previousAnswer
            }
        val childState =
            mock<PropertyRegistrationJourneyState> {
                on { getBaseJourneyState() } doReturn baseState
            }

        return WhoProvidesUpdateRoutingStepConfig().getPreviouslyDelegatedFromBaseJourney(childState)
    }
}
