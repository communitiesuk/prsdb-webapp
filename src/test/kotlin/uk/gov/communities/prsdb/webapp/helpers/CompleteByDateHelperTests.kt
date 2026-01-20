package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import kotlin.test.assertEquals

class CompleteByDateHelperTests {
    @Test
    fun `getIncompletePropertyCompleteByDate returns 28 days after savedJourneyState createdDate`() {
        val createdDate =
            LocalDate(2021, 1, 1)
                .toJavaLocalDate()
                .atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC)
        val savedJourneyState = MockSavedJourneyStateData.createSavedJourneyState(createdDate = createdDate)

        val completeByDate = CompleteByDateHelper.getIncompletePropertyCompleteByDateFromSavedJourneyState(savedJourneyState)

        assertEquals(
            LocalDate(2021, 1, 29),
            completeByDate,
        )
    }
}
