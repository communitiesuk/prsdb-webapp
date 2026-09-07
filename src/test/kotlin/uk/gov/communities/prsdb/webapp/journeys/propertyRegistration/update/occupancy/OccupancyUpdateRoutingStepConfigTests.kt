package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

class OccupancyUpdateRoutingStepConfigTests {
    @Test
    fun `mode is SHOW_INTERRUPTION when a delegated property is being made vacant`() {
        val result = modeFor(newOccupancy = YesOrNo.NO, isDelegated = true)

        assertEquals(OccupancyUpdateRouteMode.SHOW_INTERRUPTION, result)
    }

    @Test
    fun `mode is NO_INTERRUPTION when a delegated property stays occupied`() {
        val result = modeFor(newOccupancy = YesOrNo.YES, isDelegated = true)

        assertEquals(OccupancyUpdateRouteMode.NO_INTERRUPTION, result)
    }

    @Test
    fun `mode is NO_INTERRUPTION when an undelegated property is being made vacant`() {
        val result = modeFor(newOccupancy = YesOrNo.NO, isDelegated = false)

        assertEquals(OccupancyUpdateRouteMode.NO_INTERRUPTION, result)
    }

    @Test
    fun `mode is NO_INTERRUPTION when an undelegated property stays occupied`() {
        val result = modeFor(newOccupancy = YesOrNo.YES, isDelegated = false)

        assertEquals(OccupancyUpdateRouteMode.NO_INTERRUPTION, result)
    }

    @Test
    fun `mode is null when the new occupancy has not been chosen yet`() {
        val result = modeFor(newOccupancy = null, isDelegated = true)

        assertNull(result)
    }

    @Test
    fun `the delegation is not looked up when the property stays occupied`() {
        var wasLookedUp = false
        val stepConfig =
            OccupancyUpdateRoutingStepConfig().usingCurrentDelegation {
                wasLookedUp = true
                true
            }

        stepConfig.mode(stateWith(newOccupancy = YesOrNo.YES))

        assertFalse(wasLookedUp)
    }

    private fun modeFor(
        newOccupancy: YesOrNo?,
        isDelegated: Boolean,
    ): OccupancyUpdateRouteMode? =
        OccupancyUpdateRoutingStepConfig()
            .usingCurrentDelegation { isDelegated }
            .mode(stateWith(newOccupancy))

    private fun stateWith(newOccupancy: YesOrNo?): UpdateOccupancyJourneyState {
        val occupiedStep = mock<OccupiedStep> { on { outcome } doReturn newOccupancy }
        return mock<UpdateOccupancyJourneyState> { on { occupied } doReturn occupiedStep }
    }
}
