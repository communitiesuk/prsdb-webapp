package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.WhoProvidesDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

class OccupancyChangeRoutingStepConfigTests {
    private val config = OccupancyChangeRoutingStepConfig()

    @Test
    fun `mode is REMOVING_DELEGATION when previously delegated to a letting agent and now unoccupied`() {
        val result = config.mode(stateWith(YesOrNo.NO, WhoProvidesRentalDetails.LETTING_AGENT))

        assertEquals(OccupancyChangeRouteMode.REMOVING_DELEGATION, result)
    }

    @Test
    fun `mode is NO_INTERRUPTION when previously delegated but staying occupied`() {
        val result = config.mode(stateWith(YesOrNo.YES, WhoProvidesRentalDetails.LETTING_AGENT))

        assertEquals(OccupancyChangeRouteMode.NO_INTERRUPTION, result)
    }

    @Test
    fun `mode is NO_INTERRUPTION when the landlord provides details and now unoccupied`() {
        val result = config.mode(stateWith(YesOrNo.NO, WhoProvidesRentalDetails.LANDLORD))

        assertEquals(OccupancyChangeRouteMode.NO_INTERRUPTION, result)
    }

    @Test
    fun `mode is NO_INTERRUPTION when there is no cached delegation and now unoccupied`() {
        val result = config.mode(stateWith(YesOrNo.NO, null))

        assertEquals(OccupancyChangeRouteMode.NO_INTERRUPTION, result)
    }

    @Test
    fun `mode is null when the occupancy has not been submitted`() {
        val result = config.mode(stateWith(null, WhoProvidesRentalDetails.LETTING_AGENT))

        assertNull(result)
    }

    private fun stateWith(
        newOccupancy: YesOrNo?,
        cachedDelegation: WhoProvidesRentalDetails?,
    ): PropertyRegistrationJourneyState {
        val occupiedStep = mock<OccupiedStep> { on { outcome } doReturn newOccupancy }
        val whoProvidesTask = mock<WhoProvidesDetailsTask> { on { cachedWhoProvidesRentalDetails } doReturn cachedDelegation }
        return mock<PropertyRegistrationJourneyState> {
            on { occupied } doReturn occupiedStep
            on { whoProvidesDetailsTask } doReturn whoProvidesTask
        }
    }
}
