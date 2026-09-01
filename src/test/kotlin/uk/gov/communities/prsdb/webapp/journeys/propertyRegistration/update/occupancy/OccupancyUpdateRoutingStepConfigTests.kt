package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

class OccupancyUpdateRoutingStepConfigTests {
    private val stepConfig = OccupancyUpdateRoutingStepConfig()

    @Test
    fun `mode is SHOW_INTERRUPTION when a delegated property is being made vacant`() {
        val result = stepConfig.mode(stateWith(newOccupancy = YesOrNo.NO, isOccupied = true, isDelegated = true))

        assertEquals(OccupancyUpdateRouteMode.SHOW_INTERRUPTION, result)
    }

    @Test
    fun `mode is NO_INTERRUPTION when a delegated property stays occupied`() {
        val result = stepConfig.mode(stateWith(newOccupancy = YesOrNo.YES, isOccupied = true, isDelegated = true))

        assertEquals(OccupancyUpdateRouteMode.NO_INTERRUPTION, result)
    }

    @Test
    fun `mode is NO_INTERRUPTION when an undelegated property is being made vacant`() {
        val result = stepConfig.mode(stateWith(newOccupancy = YesOrNo.NO, isOccupied = true, isDelegated = false))

        assertEquals(OccupancyUpdateRouteMode.NO_INTERRUPTION, result)
    }

    @Test
    fun `mode is NO_INTERRUPTION when the property was already vacant`() {
        val result = stepConfig.mode(stateWith(newOccupancy = YesOrNo.NO, isOccupied = false, isDelegated = true))

        assertEquals(OccupancyUpdateRouteMode.NO_INTERRUPTION, result)
    }

    @Test
    fun `mode is NO_INTERRUPTION when an undelegated property was already vacant`() {
        val result = stepConfig.mode(stateWith(newOccupancy = YesOrNo.NO, isOccupied = false, isDelegated = false))

        assertEquals(OccupancyUpdateRouteMode.NO_INTERRUPTION, result)
    }

    @Test
    fun `mode is null when the new occupancy has not been chosen yet`() {
        val result = stepConfig.mode(stateWith(newOccupancy = null, isOccupied = true, isDelegated = true))

        assertNull(result)
    }

    private fun stateWith(
        newOccupancy: YesOrNo?,
        isOccupied: Boolean,
        isDelegated: Boolean,
    ): UpdateOccupancyJourneyState {
        val occupiedStep = mock<OccupiedStep> { on { outcome } doReturn newOccupancy }
        return mock<UpdateOccupancyJourneyState> {
            on { occupied } doReturn occupiedStep
            on { propertyIsOccupied } doReturn isOccupied
            on { propertyIsDelegatedToLettingAgent } doReturn isDelegated
        }
    }
}
