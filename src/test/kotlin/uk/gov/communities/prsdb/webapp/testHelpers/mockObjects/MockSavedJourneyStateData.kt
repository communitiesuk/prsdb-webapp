package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import org.springframework.test.util.ReflectionTestUtils
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.ReminderEmailSent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOneLoginUser
import java.time.Instant

class MockSavedJourneyStateData {
    companion object {
        fun createSavedJourneyState(
            journeyId: String = "journey-123",
            serializedState: String = createSerialisedStateWithSingleLineAddress("1 Example Road, EG1 2AB"),
            baseUser: OneLoginUser = createOneLoginUser(),
            reminderEmailSent: ReminderEmailSent? = null,
            createdDate: Instant = Instant.now(),
            entityId: Long = 1L,
        ): SavedJourneyState {
            val savedJourneyState =
                SavedJourneyState(
                    journeyId = journeyId,
                    serializedState = serializedState,
                    user = baseUser,
                )

            ReflectionTestUtils.setField(savedJourneyState, "createdDate", createdDate)
            ReflectionTestUtils.setField(savedJourneyState, "id", entityId)
            ReflectionTestUtils.setField(savedJourneyState, "reminderEmailSent", reminderEmailSent)

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

        fun createSerialisedStateWithManualAddress(
            addressLineOne: String = "1 Example Road",
            addressLineTwo: String? = null,
            townOrCity: String = "TownVille",
            county: String? = null,
            postcode: String = "AB1 2CD",
            localCouncilId: Int? = null,
        ): String {
            val stateData =
                mapOf(
                    "journeyData" to
                        mapOf(
                            "manual-address" to
                                mapOf(
                                    "addressLineOne" to addressLineOne,
                                    "addressLineTwo" to addressLineTwo,
                                    "townOrCity" to townOrCity,
                                    "county" to county,
                                    "postcode" to postcode,
                                ),
                            "local-council" to
                                mapOf(
                                    "localCouncilId" to localCouncilId,
                                ),
                        ),
                    "cachedAddresses" to "[]",
                )

            return ObjectMapper().writeValueAsString(stateData)
        }

        fun createReminderEmailSent(
            lastReminderEmailSentDate: Instant = Instant.now(),
            savedJourneyState: SavedJourneyState = createSavedJourneyState(),
        ): ReminderEmailSent =
            ReminderEmailSent(
                lastReminderEmailSentDate = lastReminderEmailSentDate,
                savedJourneyState = savedJourneyState,
            )
    }
}
