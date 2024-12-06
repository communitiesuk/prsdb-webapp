package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class AddressDataModelTests {
    @ParameterizedTest
    @CsvSource(
        "Flat 10,Townville,EG1 2AB,1 Example Road,Countyshire,'Flat 10, 1 Example Road, Townville, EG1 2AB, Countyshire'",
        "1 Example Road,Townville,EG1 2AB,,Countyshire,'1 Example Road, Townville, EG1 2AB, Countyshire'",
        "Flat 10,Townville,EG1 2AB,1 Example Road,,'Flat 10, 1 Example Road, Townville, EG1 2AB'",
        "1 Example Road,Townville,EG1 2AB,,,'1 Example Road, Townville, EG1 2AB'",
    )
    fun `parseSingleLineAddress returns a corresponding address string`(
        addressLineOne: String,
        townOrCity: String,
        postcode: String,
        addressLineTwo: String?,
        county: String?,
        expectedAddressString: String,
    ) {
        assertEquals(
            expectedAddressString,
            AddressDataModel.parseSingleLineAddress(addressLineOne, townOrCity, postcode, addressLineTwo, county),
        )
    }

    @Test
    fun `parseAddressDataModel returns a corresponding AddressDataModel`() {
        val expectedAddressDataModel =
            AddressDataModel(
                singleLineAddress = "Flat 10, 1 Example Road, Townville, EG1 2AB, Countyshire",
                townName = "Townville",
                postcode = "EG1 2AB",
            )

        val addressDataModel =
            AddressDataModel.parseAddressDataModel(
                "Flat 10",
                "Townville",
                "EG1 2AB",
                "1 Example Road",
                "Countyshire",
            )

        assertEquals(expectedAddressDataModel, addressDataModel)
    }
}
