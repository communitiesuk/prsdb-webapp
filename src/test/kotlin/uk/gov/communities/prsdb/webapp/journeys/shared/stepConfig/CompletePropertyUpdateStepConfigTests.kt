package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.HasPropertyId
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateSuccessBannerService

class CompletePropertyUpdateStepConfigTests {
    private val bannerService: PropertyUpdateSuccessBannerService = mock()
    private val config = CompletePropertyUpdateStepConfig(bannerService)

    private interface TestState :
        JourneyState,
        HasPropertyId

    @Test
    fun `afterStepIsReached marks success in the banner service and deletes the journey`() {
        val state: TestState = mock()
        whenever(state.propertyId).thenReturn(42L)
        whenever(state.successBannerMessageKey).thenReturn("propertyDetails.updateSuccessBanner.licensing")

        config.afterStepIsReached(state)

        verify(bannerService).markSuccess(42L, "propertyDetails.updateSuccessBanner.licensing")
        verify(state).deleteJourney()
    }

    @Test
    fun `resolveNextDestination returns the default destination unchanged`() {
        val state: TestState = mock()
        val defaultDestination = Destination.Nowhere()

        val result = config.resolveNextDestination(state, defaultDestination)

        assertEquals(defaultDestination, result)
    }
}
