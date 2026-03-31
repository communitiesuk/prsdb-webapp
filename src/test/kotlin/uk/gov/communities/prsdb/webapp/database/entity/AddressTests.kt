package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

class AddressTests {
    @Test
    fun `toMultiLineAddress returns newline-separated address when address components are available`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "Flat 1, 123 Test Street, Locality, London, SW1A 1AA",
                subBuilding = "Flat 1",
                buildingNumber = "123",
                streetName = "Test Street",
                locality = "Locality",
                townName = "London",
                postcode = "SW1A 1AA",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("Flat 1\n123 Test Street\nLocality\nLondon\nSW1A 1AA", result)
    }

    @Test
    fun `toMultiLineAddress returns newline-separated address with building name`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "The Manor House, 123 Test Street, London, SW1A 1AA",
                buildingName = "The Manor House",
                buildingNumber = "123",
                streetName = "Test Street",
                townName = "London",
                postcode = "SW1A 1AA",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("123 Test Street\nThe Manor House\nLondon\nSW1A 1AA", result)
    }

    @Test
    fun `toMultiLineAddress returns newline-separated address with organisation`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "ACME Corp, Suite 100, 123 Test Street, London, SW1A 1AA",
                organisation = "ACME Corp",
                subBuilding = "Suite 100",
                buildingNumber = "123",
                streetName = "Test Street",
                townName = "London",
                postcode = "SW1A 1AA",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("ACME Corp\nSuite 100\n123 Test Street\nLondon\nSW1A 1AA", result)
    }

    @Test
    fun `toMultiLineAddress falls back to splitting singleLineAddress when no address components`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "123 Test Street, London, SW1A 1AA",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("123 Test Street\nLondon\nSW1A 1AA", result)
    }

    @Test
    fun `toMultiLineAddress handles address with only street name component`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "Test Street, London, SW1A 1AA",
                streetName = "Test Street",
                townName = "London",
                postcode = "SW1A 1AA",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("Test Street\nLondon\nSW1A 1AA", result)
    }

    @Test
    fun `toMultiLineAddress handles address with only building name component`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "The Manor House, London, SW1A 1AA",
                buildingName = "The Manor House",
                townName = "London",
                postcode = "SW1A 1AA",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("The Manor House\nLondon\nSW1A 1AA", result)
    }
}
