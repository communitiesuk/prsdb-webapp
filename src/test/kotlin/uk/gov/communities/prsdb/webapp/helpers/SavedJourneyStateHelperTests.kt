package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import kotlin.test.assertEquals

class SavedJourneyStateHelperTests {
    @Test
    fun `getPropertyRegistrationSingleLineAddress retrieve a selected single line address from the SavedJourneyState`() {
        // Arrange
        val singleLineAddress = "1 Example Road, EG1 2AB"
        val savedJourneyState =
            MockSavedJourneyStateData.Companion.createSavedJourneyState(
                serializedState = MockSavedJourneyStateData.Companion.createSerialisedStateWithSingleLineAddress(singleLineAddress),
            )

        // Act
        val result = SavedJourneyStateHelper.getPropertyRegistrationSingleLineAddress(savedJourneyState.serializedState)

        // Assert
        assertEquals(singleLineAddress, result)
    }

    @Test
    fun `getPropertyRegistrationSingleLineAddress returns a manual single line address from the SavedJourneyState`() {
        // Arrange
        val savedJourneyState =
            MockSavedJourneyStateData.Companion.createSavedJourneyState(
                serializedState =
                    MockSavedJourneyStateData.Companion.createSerialisedStateWithManualAddress(
                        addressLineOne = "1 Example Road",
                        townOrCity = "TownVille",
                        postcode = "EG1 2AB",
                    ),
            )
        val expectedSingleLineAddress = "1 Example Road, TownVille, EG1 2AB"

        // Act
        val result = SavedJourneyStateHelper.getPropertyRegistrationSingleLineAddress(savedJourneyState.serializedState)

        // Assert
        assertEquals(expectedSingleLineAddress, result)
    }
}
