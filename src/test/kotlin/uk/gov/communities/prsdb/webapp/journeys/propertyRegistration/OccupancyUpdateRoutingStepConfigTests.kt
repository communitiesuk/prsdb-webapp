package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.OccupancyUpdateRouteMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.OccupancyUpdateRoutingStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

class OccupancyUpdateRoutingStepConfigTests {
    private val stepConfig = OccupancyUpdateRoutingStepConfig()

    @Test
    fun `mode is REMOVING_DELEGATION when a delegated property is being made vacant`() {
        val result = stepConfig.mode(stateWith(newOccupancy = YesOrNo.NO, showsInterruption = true))

        assertEquals(OccupancyUpdateRouteMode.REMOVING_DELEGATION, result)
    }

    @Test
    fun `mode is NO_INTERRUPTION when a delegated property stays occupied`() {
        val result = stepConfig.mode(stateWith(newOccupancy = YesOrNo.YES, showsInterruption = true))

        assertEquals(OccupancyUpdateRouteMode.NO_INTERRUPTION, result)
    }

    @Test
    fun `mode is NO_INTERRUPTION when an undelegated property is being made vacant`() {
        val result = stepConfig.mode(stateWith(newOccupancy = YesOrNo.NO, showsInterruption = false))

        assertEquals(OccupancyUpdateRouteMode.NO_INTERRUPTION, result)
    }

    @Test
    fun `mode is null when the new occupancy has not been chosen yet`() {
        val result = stepConfig.mode(stateWith(newOccupancy = null, showsInterruption = true))

        assertNull(result)
    }

    private fun stateWith(
        newOccupancy: YesOrNo?,
        showsInterruption: Boolean,
    ): UpdateOccupancyJourneyState {
        val occupiedStep = mock<OccupiedStep> { on { outcome } doReturn newOccupancy }
        return mock<UpdateOccupancyJourneyState> {
            on { occupied } doReturn occupiedStep
            on { showsLettingAgentInterruption } doReturn showsInterruption
        }
    }
}
