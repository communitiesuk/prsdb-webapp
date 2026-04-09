package uk.gov.communities.prsdb.webapp.helpers.extensions.savedJourneyDataExtensions

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.helpers.extensions.savedJourneyStateExtensions.SavedJourneyStateExtensions.Companion.getPropertyRegistrationSingleLineAddress
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockSavedJourneyStateData
import kotlin.test.assertEquals

class SavedJourneyDataExtensionsTests {
    @Test
    fun `getPropertyRegistrationSingleLineAddress retrieve a selected single line address from the SavedJourneyState`() {
        // Arrange
        val singleLineAddress = "1 Example Road, EG1 2AB"
        val savedJourneyState =
            MockSavedJourneyStateData.createSavedJourneyState(
                serializedState = MockSavedJourneyStateData.createSerialisedStateWithSingleLineAddress(singleLineAddress),
            )

        // Act
        val result = savedJourneyState.getPropertyRegistrationSingleLineAddress()

        // Assert
        assertEquals(singleLineAddress, result)
    }

    @Test
    fun `getPropertyRegistrationSingleLineAddress returns a manual single line address from the SavedJourneyState`() {
        // Arrange
        val savedJourneyState =
            MockSavedJourneyStateData.createSavedJourneyState(
                serializedState =
                    MockSavedJourneyStateData.createSerialisedStateWithManualAddress(
                        addressLineOne = "1 Example Road",
                        townOrCity = "TownVille",
                        postcode = "EG1 2AB",
                    ),
            )
        val expectedSingleLineAddress = "1 Example Road, TownVille, EG1 2AB"

        // Act
        val result = savedJourneyState.getPropertyRegistrationSingleLineAddress()

        // Assert
        assertEquals(expectedSingleLineAddress, result)
    }
}
