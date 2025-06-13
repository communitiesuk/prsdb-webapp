package uk.gov.communities.prsdb.webapp.helpers.extensions

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.NonStepJourneyDataKey
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getLookedUpAddress
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getLookedUpAddresses
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getSerializedLookedUpAddresses
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.withUpdatedLookedUpAddresses
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

        val journeyData = mapOf(NonStepJourneyDataKey.LookedUpAddresses.key to Json.encodeToString(lookedUpAddresses))

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

        val journeyData = mapOf(NonStepJourneyDataKey.LookedUpAddresses.key to Json.encodeToString(lookedUpAddresses))

        val retrievedAddress = journeyData.getLookedUpAddress(requestedSingleLineAddress)

        assertNull(retrievedAddress)
    }

    @Test
    fun `getLookedUpAddresses returns the looked-up AddressDataModels from journeyData`() {
        val lookedUpAddresses =
            listOf(
                AddressDataModel("1, Example Road, EG", 1, 1234, buildingNumber = "1", postcode = "EG"),
                AddressDataModel("2, Example Road, EG", 2, buildingNumber = "2", postcode = "EG"),
                AddressDataModel("Main, Example Road, EG", 3, buildingName = "Main", postcode = "EG"),
            )

        val journeyData = mapOf(NonStepJourneyDataKey.LookedUpAddresses.key to Json.encodeToString(lookedUpAddresses))

        val retrievedAddresses = journeyData.getLookedUpAddresses()

        assertEquals(lookedUpAddresses, retrievedAddresses)
    }

    @Test
    fun `getLookedUpAddresses returns an empty list when there aren't looked-up addresses in journeyData`() {
        val journeyData = mapOf("other-key" to "other-value")

        val retrievedAddresses = journeyData.getLookedUpAddresses()

        assertEquals(emptyList(), retrievedAddresses)
    }

    @Test
    fun `getSerializedLookedUpAddresses returns the serialized looked-up addresses in journeyData`() {
        val serializedAddresses = "serialized-looked-up-addresses"
        val journeyData = mapOf(NonStepJourneyDataKey.LookedUpAddresses.key to serializedAddresses)

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
        val expectedUpdatedJourneyData = journeyData + (NonStepJourneyDataKey.LookedUpAddresses.key to serializedAddresses)

        val updatedJourneyData = journeyData.withUpdatedLookedUpAddresses(serializedAddresses)

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
        val expectedUpdatedJourneyData = journeyData + (NonStepJourneyDataKey.LookedUpAddresses.key to Json.encodeToString(addresses))

        val updatedJourneyData = journeyData.withUpdatedLookedUpAddresses(addresses)

        assertEquals(expectedUpdatedJourneyData, updatedJourneyData)
    }
}
