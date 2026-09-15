package uk.gov.communities.prsdb.webapp.helpers.extensions.savedJourneyDataExtensions

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.communities.prsdb.webapp.helpers.extensions.savedJourneyStateExtensions.SavedJourneyStateExtensions.Companion.getPropertyRegistrationMultiLineAddress
import uk.gov.communities.prsdb.webapp.helpers.extensions.savedJourneyStateExtensions.SavedJourneyStateExtensions.Companion.getPropertyRegistrationSingleLineAddress
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
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

    @Test
    fun `getPropertyRegistrationMultiLineAddress retrieves a selected address as multiple lines from the SavedJourneyState`() {
        // Arrange
        val selectedAddress =
            AddressDataModel(
                singleLineAddress = "2 Example House, Example Road, Example District, Example Town, EG1 2AB",
                buildingName = "Example House",
                buildingNumber = "2",
                streetName = "Example Road",
                locality = "Example District",
                townName = "Example Town",
                postcode = "EG1 2AB",
            )
        val savedJourneyState =
            MockSavedJourneyStateData.createSavedJourneyState(
                serializedState = createSerialisedStateWithSelectedAddress(selectedAddress),
            )

        // Act
        val result = savedJourneyState.getPropertyRegistrationMultiLineAddress()

        // Assert
        assertEquals(
            "2\nExample House\nExample Road\nExample District\nExample Town\nEG1 2AB",
            result,
        )
    }

    @Test
    fun `getPropertyRegistrationMultiLineAddress returns a manual address as multiple lines from the SavedJourneyState`() {
        // Arrange
        val savedJourneyState =
            MockSavedJourneyStateData.createSavedJourneyState(
                serializedState =
                    MockSavedJourneyStateData.createSerialisedStateWithManualAddress(
                        addressLineOne = "1 Example Road",
                        addressLineTwo = "Flat 1",
                        townOrCity = "TownVille",
                        county = "Example County",
                        postcode = "EG1 2AB",
                        localCouncilId = 22,
                    ),
            )

        // Act
        val result = savedJourneyState.getPropertyRegistrationMultiLineAddress()

        // Assert
        assertEquals("1 Example Road\nFlat 1\nTownVille\nExample County\nEG1 2AB", result)
    }

    private fun createSerialisedStateWithSelectedAddress(address: AddressDataModel): String {
        val stateData =
            mapOf(
                "journeyData" to
                    mapOf(
                        "select-address" to
                            mapOf(
                                "address" to address.singleLineAddress,
                            ),
                    ),
                "cachedAddresses" to Json.encodeToString(listOf(address)),
            )

        return ObjectMapper().writeValueAsString(stateData)
    }
}
