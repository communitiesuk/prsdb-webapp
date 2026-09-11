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

        assertEquals("123\nThe Manor House\nTest Street\nLondon\nSW1A 1AA", result)
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

    @Test
    fun `toMultiLineAddress combines building number and street name on one line when there is no building name`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "10 Sample Street, Sampleton, AB1 2CD",
                buildingNumber = "10",
                streetName = "Sample Street",
                townName = "Sampleton",
                postcode = "AB1 2CD",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("10 Sample Street\nSampleton\nAB1 2CD", result)
    }

    @Test
    fun `toMultiLineAddress orders building number then building name then street name`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "3, Willow Court, Meadow Lane, Sampleton, AB1 2CD",
                buildingNumber = "3",
                buildingName = "Willow Court",
                streetName = "Meadow Lane",
                townName = "Sampleton",
                postcode = "AB1 2CD",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("3\nWillow Court\nMeadow Lane\nSampleton\nAB1 2CD", result)
    }

    @Test
    fun `toMultiLineAddress shows building name then street name when there is no building number`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "Willow Court, Meadow Lane, Sampleton, AB1 2CD",
                buildingName = "Willow Court",
                streetName = "Meadow Lane",
                townName = "Sampleton",
                postcode = "AB1 2CD",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("Willow Court\nMeadow Lane\nSampleton\nAB1 2CD", result)
    }

    @Test
    fun `toMultiLineAddress includes sub-building before building number, name and street`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "Flat 2, 3, Willow Court, Meadow Lane, Sampleton, AB1 2CD",
                subBuilding = "Flat 2",
                buildingNumber = "3",
                buildingName = "Willow Court",
                streetName = "Meadow Lane",
                townName = "Sampleton",
                postcode = "AB1 2CD",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("Flat 2\n3\nWillow Court\nMeadow Lane\nSampleton\nAB1 2CD", result)
    }

    @Test
    fun `toMultiLineAddress includes organisation before building number, name and street`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "Sample Org, 3, Willow Court, Meadow Lane, Sampleton, AB1 2CD",
                organisation = "Sample Org",
                buildingNumber = "3",
                buildingName = "Willow Court",
                streetName = "Meadow Lane",
                townName = "Sampleton",
                postcode = "AB1 2CD",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("Sample Org\n3\nWillow Court\nMeadow Lane\nSampleton\nAB1 2CD", result)
    }

    @Test
    fun `toMultiLineAddress includes locality between street and town when present`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "3, Willow Court, Meadow Lane, Sample Locality, Sampleton, AB1 2CD",
                buildingNumber = "3",
                buildingName = "Willow Court",
                streetName = "Meadow Lane",
                locality = "Sample Locality",
                townName = "Sampleton",
                postcode = "AB1 2CD",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("3\nWillow Court\nMeadow Lane\nSample Locality\nSampleton\nAB1 2CD", result)
    }

    @Test
    fun `toMultiLineAddress falls back to splitting singleLineAddress for manually entered addresses`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "12 Sample Road, Sample District, Sampleton, AB1 2CD",
            )
        val address = Address(addressDataModel)

        val result = address.toMultiLineAddress()

        assertEquals("12 Sample Road\nSample District\nSampleton\nAB1 2CD", result)
    }
}
