package uk.gov.communities.prsdb.webapp.helpers.extensions

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.helpers.extensions.JourneyDataExtensions.Companion.getLookedUpAddress
import uk.gov.communities.prsdb.webapp.helpers.extensions.JourneyDataExtensions.Companion.getSerializedLookedUpAddresses
import uk.gov.communities.prsdb.webapp.helpers.extensions.JourneyDataExtensions.Companion.updateLookedUpAddresses
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JourneyDataExtensionsTests {
    @Test
    fun `getLookedUpAddress retrieves the requested AddressDataModel from journeyData`() {
        val requestedAddress = AddressDataModel("1, Example Road, EG", 1, 1234, buildingNumber = "1", postcode = "EG")

        val lookedUpAddresses =
            listOf(
                requestedAddress,
                AddressDataModel("2, Example Road, EG", 2, buildingNumber = "2", postcode = "EG"),
                AddressDataModel("Main, Example Road, EG", 3, buildingName = "Main", postcode = "EG"),
            )

        val journeyData = mapOf(LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY to Json.encodeToString(lookedUpAddresses))

        val retrievedAddress = journeyData.getLookedUpAddress(requestedAddress.singleLineAddress)

        assertEquals(requestedAddress, retrievedAddress)
    }

    @Test
    fun `getLookedUpAddress returns null when the requested AddressDataModel is not in journeyData`() {
        val requestedSingleLineAddress = "address not in journeyData"

        val lookedUpAddresses =
            listOf(
                AddressDataModel("1, Example Road, EG", 1, 1234, buildingNumber = "1", postcode = "EG"),
                AddressDataModel("2, Example Road, EG", 2, buildingNumber = "2", postcode = "EG"),
                AddressDataModel("Main, Example Road, EG", 3, buildingName = "Main", postcode = "EG"),
            )

        val journeyData = mapOf(LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY to Json.encodeToString(lookedUpAddresses))

        val retrievedAddress = journeyData.getLookedUpAddress(requestedSingleLineAddress)

        assertNull(retrievedAddress)
    }

    @Test
    fun `getSerializedLookedUpAddresses returns the serialized looked-up addresses in journeyData`() {
        val serializedAddresses = "serialized-looked-up-addresses"
        val journeyData = mapOf(LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY to serializedAddresses)

        val retrievedSerializedAddresses = journeyData.getSerializedLookedUpAddresses()

        assertEquals(serializedAddresses, retrievedSerializedAddresses)
    }

    @Test
    fun `getSerializedLookedUpAddresses returns null when there aren't serialized looked-up addresses in journeyData`() {
        val journeyData = mapOf("other-key" to "other-value")

        val retrievedSerializedAddresses = journeyData.getSerializedLookedUpAddresses()

        assertNull(retrievedSerializedAddresses)
    }

    @Test
    fun `updateLookedUpAddresses returns journeyData updated with the given serialized addresses`() {
        val serializedAddresses = "serialized-looked-up-addresses"
        val journeyData = mapOf("other-key" to "other-value")
        val expectedUpdatedJourneyData = journeyData + (LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY to serializedAddresses)

        val updatedJourneyData = journeyData.updateLookedUpAddresses(serializedAddresses)

        assertEquals(expectedUpdatedJourneyData, updatedJourneyData)
    }

    @Test
    fun `updateLookedUpAddresses returns journeyData updated with the given addresses`() {
        val addresses =
            listOf(
                AddressDataModel("1, Example Road, EG", 1, 1234, buildingNumber = "1", postcode = "EG"),
                AddressDataModel("2, Example Road, EG", 2, buildingNumber = "2", postcode = "EG"),
                AddressDataModel("Main, Example Road, EG", 3, buildingName = "Main", postcode = "EG"),
            )
        val journeyData = mapOf("other-key" to "other-value")
        val expectedUpdatedJourneyData = journeyData + (LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY to Json.encodeToString(addresses))

        val updatedJourneyData = journeyData.updateLookedUpAddresses(addresses)

        assertEquals(expectedUpdatedJourneyData, updatedJourneyData)
    }
}
