package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

class OccupancyChangeRoutingStepConfigTests {
    @Nested
    inner class ModeTests {
        @Test
        fun `mode is REMOVING_DELEGATION when previously delegated to a letting agent and now unoccupied`() {
            val result = configuredConfig(wasDelegated = true).mode(stateWith(YesOrNo.NO))

            assertEquals(OccupancyChangeRouteMode.REMOVING_DELEGATION, result)
        }

        @Test
        fun `mode is NO_INTERRUPTION when previously delegated but staying occupied`() {
            val result = configuredConfig(wasDelegated = true).mode(stateWith(YesOrNo.YES))

            assertEquals(OccupancyChangeRouteMode.NO_INTERRUPTION, result)
        }

        @Test
        fun `mode is NO_INTERRUPTION when not previously delegated and now unoccupied`() {
            val result = configuredConfig(wasDelegated = false).mode(stateWith(YesOrNo.NO))

            assertEquals(OccupancyChangeRouteMode.NO_INTERRUPTION, result)
        }
    }

    @Nested
    inner class PreviousDelegationTests {
        @Test
        fun `base journey lookup is true when originally occupied and delegated to a letting agent`() {
            val result = getWasDelegatedFromBaseJourney(cachedOccupied = true, cachedDelegation = WhoProvidesRentalDetails.LETTING_AGENT)

            assertTrue(result)
        }

        @Test
        fun `base journey lookup is false when originally occupied but the landlord provided details`() {
            val result = getWasDelegatedFromBaseJourney(cachedOccupied = true, cachedDelegation = WhoProvidesRentalDetails.LANDLORD)

            assertFalse(result)
        }

        @Test
        fun `base journey lookup is false when originally occupied with no cached delegation`() {
            val result = getWasDelegatedFromBaseJourney(cachedOccupied = true, cachedDelegation = null)

            assertFalse(result)
        }

        @Test
        fun `base journey lookup is false when the property was already unoccupied even with a stale delegation`() {
            val result = getWasDelegatedFromBaseJourney(cachedOccupied = false, cachedDelegation = WhoProvidesRentalDetails.LETTING_AGENT)

            assertFalse(result)
        }

        @Test
        fun `base journey lookup is false when the previous occupancy is unset`() {
            val result = getWasDelegatedFromBaseJourney(cachedOccupied = null, cachedDelegation = WhoProvidesRentalDetails.LETTING_AGENT)

            assertFalse(result)
        }
    }

    private fun configuredConfig(wasDelegated: Boolean) =
        OccupancyChangeRoutingStepConfig().apply {
            usingPreviousDelegation { wasDelegated }
        }

    private fun stateWith(newOccupancy: YesOrNo?): PropertyRegistrationJourneyState {
        val occupiedStep = mock<OccupiedStep> { on { outcome } doReturn newOccupancy }
        return mock<PropertyRegistrationJourneyState> { on { occupied } doReturn occupiedStep }
    }

    private fun getWasDelegatedFromBaseJourney(
        cachedOccupied: Boolean?,
        cachedDelegation: WhoProvidesRentalDetails?,
    ): Boolean {
        val baseState =
            mock<PropertyRegistrationJourneyState> {
                on { this.cachedOccupied } doReturn cachedOccupied
                on { this.cachedWhoProvidesRentalDetails } doReturn cachedDelegation
            }
        val childState = mock<PropertyRegistrationJourneyState> { on { getBaseJourneyState() } doReturn baseState }

        return OccupancyChangeRoutingStepConfig().getWasDelegatedToLettingAgentFromBaseJourney(childState)
    }
}
