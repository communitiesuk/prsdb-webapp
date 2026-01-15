package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import org.springframework.test.util.ReflectionTestUtils
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOneLoginUser
import java.time.Instant

class MockSavedJourneyStateData {
    companion object {
        fun createSavedJourneyState(
            journeyId: String = "journey-123",
            serializedState: String = createSerialisedStateWithSingleLineAddress("1 Example Road, EG1 2AB"),
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

        fun createSerialisedStateWithSingleLineAddress(singleLineAddress: String): String {
            val stateData =
                mapOf(
                    "journeyData" to
                        mapOf(
                            "select-address" to
                                mapOf(
                                    "address" to singleLineAddress,
                                ),
                        ),
                    "cachedAddresses" to "[{\"singleLineAddress\" : \"${singleLineAddress}\"}]",
                )

            return ObjectMapper().writeValueAsString(stateData)
        }

        fun createLandlordIncompleteProperties(
            landlord: Landlord = createLandlord(),
            savedJourneyState: SavedJourneyState = createSavedJourneyState(),
        ): LandlordIncompleteProperties =
            LandlordIncompleteProperties(
                landlord = landlord,
                savedJourneyState = savedJourneyState,
            )
    }
}
