package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState

class FinishCyaJourneyConfigTests {
    private val config = FinishCyaJourneyConfig()
    private val mockState = mock<CheckYourAnswersJourneyState>()
    private val mockOriginalState = mock<CheckYourAnswersJourneyState>()

    private val baseJourneyId = "base-journey-id"
    private val cyaRouteSegment = "lookup-address"

    @BeforeEach
    fun setUp() {
        whenever(mockState.baseJourneyId).thenReturn(baseJourneyId)
        whenever(mockState.getBaseJourneyState()).thenReturn(mockOriginalState)
        whenever(mockState.checkingAnswersFor).thenReturn(cyaRouteSegment)
    }

    @Nested
    inner class AfterStepIsReached {
        private val snapshot = "cya-answers-snapshot"

        @Test
        fun `copies CYA journey data to the base journey when the answers shown on CYA have not changed`() {
            whenever(mockState.originalCyaAnswers).thenReturn(snapshot)
            whenever(mockOriginalState.cyaAnswersMatchSnapshot(snapshot)).thenReturn(true)

            config.afterStepIsReached(mockState)

            verify(mockState).copyJourneyTo(baseJourneyId)
        }

        @Test
        fun `ensures CYA fields are not present in the parent state that will be saved`() {
            whenever(mockState.originalCyaAnswers).thenReturn(snapshot)
            whenever(mockOriginalState.cyaAnswersMatchSnapshot(snapshot)).thenReturn(true)

            config.afterStepIsReached(mockState)

            verify(mockOriginalState).clearCyaFields()
        }

        @Test
        fun `throws CyaDataHasChangedException and does not copy when the answers shown on CYA have changed`() {
            whenever(mockState.originalCyaAnswers).thenReturn(snapshot)
            whenever(mockOriginalState.cyaAnswersMatchSnapshot(snapshot)).thenReturn(false)

            assertThrows<CyaDataHasChangedException> {
                config.afterStepIsReached(mockState)
            }
            verify(mockState, never()).copyJourneyTo(baseJourneyId)
        }

        @Test
        fun `throws CyaDataHasChangedException and does not copy when there is no CYA answers snapshot`() {
            whenever(mockState.originalCyaAnswers).thenReturn(null)

            assertThrows<CyaDataHasChangedException> {
                config.afterStepIsReached(mockState)
            }
            verify(mockState, never()).copyJourneyTo(baseJourneyId)
        }
    }

    @Nested
    inner class SaveState {
        @Test
        fun `saves the base journey state to the database`() {
            val mockSavedState = mock<SavedJourneyState>()
            whenever(mockOriginalState.save()).thenReturn(mockSavedState)

            config.saveState(mockState)

            verify(mockOriginalState).save()
        }
    }

    @Nested
    inner class ResolveNextDestination {
        @Test
        fun `returns the CYA page destination`() {
            val cyaDestination = Destination.StepRoute("cya-step", baseJourneyId)
            whenever(mockState.returnToCyaPageDestination).thenReturn(cyaDestination)
            whenever(mockOriginalState.cyaJourneys).thenReturn(mapOf(cyaRouteSegment to "cya-journey-id"))

            val result = config.resolveNextDestination(mockState, Destination.Nowhere())

            assertEquals(cyaDestination, result)
        }

        @Test
        fun `clears the checkingAnswersFor on the original state`() {
            whenever(mockState.returnToCyaPageDestination).thenReturn(Destination.Nowhere())
            whenever(mockOriginalState.cyaJourneys).thenReturn(mapOf(cyaRouteSegment to "cya-journey-id"))

            config.resolveNextDestination(mockState, Destination.Nowhere())

            verify(mockOriginalState).checkingAnswersFor = null
        }

        @Test
        fun `removes the CYA journey entry from the original state`() {
            whenever(mockState.returnToCyaPageDestination).thenReturn(Destination.Nowhere())
            whenever(mockOriginalState.cyaJourneys).thenReturn(mapOf(cyaRouteSegment to "cya-journey-id"))

            config.resolveNextDestination(mockState, Destination.Nowhere())

            verify(mockOriginalState, atLeastOnce()).cyaJourneys = emptyMap()
        }

        @Test
        fun `deletes the CYA journey`() {
            whenever(mockState.returnToCyaPageDestination).thenReturn(Destination.Nowhere())
            whenever(mockOriginalState.cyaJourneys).thenReturn(mapOf(cyaRouteSegment to "cya-journey-id"))

            config.resolveNextDestination(mockState, Destination.Nowhere())

            verify(mockState).deleteJourney()
        }
    }
}
