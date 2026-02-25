package uk.gov.communities.prsdb.webapp.journeys

import jakarta.servlet.ServletRequest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JourneyIdProviderTests {
    @Test
    fun `getParameterOrNull returns the journeyId parameter value when present`() {
        // Arrange
        val expectedJourneyId = "test-journey-id"
        val request = mock<ServletRequest>()
        whenever(request.getParameter("journeyId")).thenReturn(expectedJourneyId)
        val provider = JourneyIdProvider(request)

        // Act
        val actualJourneyId = provider.getParameterOrNull()

        // Assert
        assertEquals(expectedJourneyId, actualJourneyId)
    }

    @Test
    fun `getParameterOrNull returns null when the journeyId parameter is absent`() {
        // Arrange
        val request = mock<ServletRequest>()
        whenever(request.getParameter("journeyId")).thenReturn(null)
        val provider = JourneyIdProvider(request)

        // Act
        val result = provider.getParameterOrNull()

        // Assert
        assertNull(result)
    }
}
