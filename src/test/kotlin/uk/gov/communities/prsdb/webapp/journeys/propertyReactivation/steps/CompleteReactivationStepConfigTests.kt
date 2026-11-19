package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState

@ExtendWith(MockitoExtension::class)
class CompleteReactivationStepConfigTests {
    @Mock
    lateinit var mockState: PropertyReactivationJourneyState

    @Test
    fun `resolveNextDestination deletes the journey`() {
        val stepConfig = CompleteReactivationStepConfig()

        stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))

        verify(mockState).deleteJourney()
    }
}
