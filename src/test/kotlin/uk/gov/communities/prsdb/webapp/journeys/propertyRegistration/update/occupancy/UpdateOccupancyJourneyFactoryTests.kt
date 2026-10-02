package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal
import java.time.Instant

class UpdateOccupancyJourneyFactoryTests {
    @Test
    fun `initializeJourneyState reinitialises outdated state and returns the journey id`() {
        val seed = Pair(1L, Principal { "user" })
        val currentLastModifiedDate = Instant.parse("2026-09-22T10:00:00Z")
        val journeyId = "occupancy-journey-id"
        val state =
            mock<UpdateOccupancyJourney> {
                on {
                    initialiseOrRestoreStateReinitialisingIfOutdated(seed, currentLastModifiedDate)
                } doReturn journeyId
            }
        val stateFactory =
            mock<ObjectFactory<UpdateOccupancyJourney>> {
                on { getObject() } doReturn state
            }
        val factory =
            UpdateOccupancyJourneyFactory(
                stateFactory,
                mock<PropertyOwnershipService>(),
                mock<FeatureFlagManager>(),
            )

        val result = factory.initializeJourneyState(seed, currentLastModifiedDate)

        assertEquals(journeyId, result)
        verify(state).initialiseOrRestoreStateReinitialisingIfOutdated(seed, currentLastModifiedDate)
    }
}
