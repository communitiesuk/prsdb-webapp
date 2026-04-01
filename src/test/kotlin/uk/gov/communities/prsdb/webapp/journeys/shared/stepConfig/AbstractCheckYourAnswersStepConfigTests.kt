package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyMetadata
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState

@ExtendWith(MockitoExtension::class)
class AbstractCheckYourAnswersStepConfigTests {
    private val stepConfig =
        object : AbstractCheckYourAnswersStepConfig<CheckYourAnswersJourneyState>() {
            override fun getStepSpecificContent(state: CheckYourAnswersJourneyState): Map<String, Any?> = emptyMap()
        }

    @Mock
    private lateinit var mockState: CheckYourAnswersJourneyState

    @Nested
    inner class ResolveNextDestination {
        @Test
        fun `deletes the journey and returns the default destination when the journey has no parent`() {
            whenever(mockState.journeyMetadata).thenReturn(JourneyMetadata.createNew("journey-1"))
            val destination = Destination.Nowhere()

            val result = stepConfig.resolveNextDestination(mockState, destination)

            verify(mockState).deleteJourney()
            verify(mockState, never()).getBaseJourneyState()
            assertEquals(destination, result)
        }

        @Test
        fun `deletes both the child and parent journey when the journey is a child`() {
            val mockParentState = mock<CheckYourAnswersJourneyState>()
            whenever(mockState.journeyMetadata).thenReturn(
                JourneyMetadata.createNew("child-1", baseJourneyId = "parent-1"),
            )
            whenever(mockState.getBaseJourneyState()).thenReturn(mockParentState)
            val destination = Destination.Nowhere()

            val result = stepConfig.resolveNextDestination(mockState, destination)

            val deleteOrder = inOrder(mockState, mockParentState)
            deleteOrder.verify(mockState).deleteJourney()
            deleteOrder.verify(mockParentState).deleteJourney()
            assertEquals(destination, result)
        }
    }
}
