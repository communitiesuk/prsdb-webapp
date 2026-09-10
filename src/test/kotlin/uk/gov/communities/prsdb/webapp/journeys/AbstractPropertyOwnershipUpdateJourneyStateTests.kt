package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AbstractPropertyOwnershipUpdateJourneyStateTests {
    @Test
    fun `discardIfLastModifiedDateChanged discards the journey when the stored last modified date differs from the current one`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val journeyState = TestPropertyOwnershipUpdateJourneyState(journeyStateService)
        val seed = PropertyOwnershipUpdateJourneySeed(1L)
        val journeyId = journeyState.generateJourneyId(seed)
        whenever(journeyStateService.getStoredStringValueOrNull(journeyId, "lastModifiedDate")).thenReturn("t0")

        // Act
        journeyState.discardIfLastModifiedDateChanged(seed, "t1")

        // Assert
        verify(journeyStateService).discardJourney(journeyId)
    }

    @Test
    fun `discardIfLastModifiedDateChanged does not discard the journey when the stored last modified date matches the current one`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val journeyState = TestPropertyOwnershipUpdateJourneyState(journeyStateService)
        val seed = PropertyOwnershipUpdateJourneySeed(1L)
        val journeyId = journeyState.generateJourneyId(seed)
        whenever(journeyStateService.getStoredStringValueOrNull(journeyId, "lastModifiedDate")).thenReturn("t1")

        // Act
        journeyState.discardIfLastModifiedDateChanged(seed, "t1")

        // Assert
        verify(journeyStateService, never()).discardJourney(any())
    }

    @Test
    fun `discardIfLastModifiedDateChanged does not discard the journey when there is no stored last modified date`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val journeyState = TestPropertyOwnershipUpdateJourneyState(journeyStateService)
        val seed = PropertyOwnershipUpdateJourneySeed(1L)
        val journeyId = journeyState.generateJourneyId(seed)
        whenever(journeyStateService.getStoredStringValueOrNull(journeyId, "lastModifiedDate")).thenReturn(null)

        // Act
        journeyState.discardIfLastModifiedDateChanged(seed, "t1")

        // Assert
        verify(journeyStateService, never()).discardJourney(any())
    }
}

private class TestPropertyOwnershipUpdateJourneyState(
    journeyStateService: JourneyStateService,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, "TestJourney")
