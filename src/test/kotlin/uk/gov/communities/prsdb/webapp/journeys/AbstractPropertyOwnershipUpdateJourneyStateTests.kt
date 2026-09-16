package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class AbstractPropertyOwnershipUpdateJourneyStateTests {
    private val storedDate = Instant.parse("2020-01-01T00:00:00Z")
    private val currentDate = Instant.parse("2021-01-01T00:00:00Z")

    @Test
    fun `discardIfLastModifiedDateChanged discards the journey when the stored last modified date differs from the current one`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val journeyState = TestPropertyOwnershipUpdateJourneyState(journeyStateService)
        val seed = UUID.randomUUID()
        val journeyId = journeyState.generateJourneyId(seed)
        whenever(journeyStateService.getStoredStringValueOrNull(journeyId, "lastModifiedDate")).thenReturn(storedDate.toString())

        // Act
        journeyState.discardIfLastModifiedDateChanged(seed, currentDate)

        // Assert
        verify(journeyStateService).deleteState(journeyId)
    }

    @Test
    fun `discardIfLastModifiedDateChanged does not discard the journey when the stored last modified date matches the current one`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val journeyState = TestPropertyOwnershipUpdateJourneyState(journeyStateService)
        val seed = UUID.randomUUID()
        val journeyId = journeyState.generateJourneyId(seed)
        whenever(journeyStateService.getStoredStringValueOrNull(journeyId, "lastModifiedDate")).thenReturn(currentDate.toString())

        // Act
        journeyState.discardIfLastModifiedDateChanged(seed, currentDate)

        // Assert
        verify(journeyStateService, never()).deleteState(any())
    }

    @Test
    fun `discardIfLastModifiedDateChanged does not discard the journey when there is no stored last modified date`() {
        // Arrange
        val journeyStateService: JourneyStateService = mock()
        val journeyState = TestPropertyOwnershipUpdateJourneyState(journeyStateService)
        val seed = UUID.randomUUID()
        val journeyId = journeyState.generateJourneyId(seed)
        whenever(journeyStateService.getStoredStringValueOrNull(journeyId, "lastModifiedDate")).thenReturn(null)

        // Act
        journeyState.discardIfLastModifiedDateChanged(seed, currentDate)

        // Assert
        verify(journeyStateService, never()).deleteState(any())
    }
}

private class TestPropertyOwnershipUpdateJourneyState(
    journeyStateService: JourneyStateService,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, "TestJourney")
