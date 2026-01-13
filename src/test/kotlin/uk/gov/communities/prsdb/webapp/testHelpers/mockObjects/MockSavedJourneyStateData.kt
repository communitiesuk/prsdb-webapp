package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOneLoginUser
import java.time.Instant

class MockSavedJourneyStateData {
    companion object {
        fun createSavedJourneyState(
            journeyId: String = "journey-123",
            serializedState: String = """{"key":"value"}""",
            baseUser: OneLoginUser = createOneLoginUser(),
            createdDate: Instant = Instant.now(),
        ): SavedJourneyState {
            val savedJourneyState =
                SavedJourneyState(
                    journeyId = journeyId,
                    serializedState = serializedState,
                    user = baseUser,
                )

            ReflectionTestUtils.setField(savedJourneyState, "createdDate", createdDate)

            return savedJourneyState
        }
    }
}
