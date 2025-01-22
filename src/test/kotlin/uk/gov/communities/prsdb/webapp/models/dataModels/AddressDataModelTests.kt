package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class AddressDataModelTests {
    companion object {
        @JvmStatic
        fun provideAddresses() =
            listOf(
                Arguments.of(
                    Named.of(
                        "no fields blank or missing",
                        "Flat 10, 1 Example Road, Townville, Countyshire, EG1 2AB",
                    ),
                    "Flat 10",
                    "Townville",
                    "EG1 2AB",
                    "1 Example Road",
                    "Countyshire",
                ),
                Arguments.of(
                    Named.of(
                        "addressLineTwo blank",
                        "1 Example Road, Townville, Countyshire, EG1 2AB",
                    ),
                    "1 Example Road",
                    "Townville",
                    "EG1 2AB",
                    "",
                    "Countyshire",
                ),
                Arguments.of(
                    Named.of(
                        "county blank",
                        "Flat 10, 1 Example Road, Townville, EG1 2AB",
                    ),
                    "Flat 10",
                    "Townville",
                    "EG1 2AB",
                    "1 Example Road",
                    "",
                ),
                Arguments.of(
                    Named.of(
                        "addressLineTwo and county missing",
                        "1 Example Road, Townville, EG1 2AB",
                    ),
                    "1 Example Road",
                    "Townville",
                    "EG1 2AB",
                    null,
                    null,
                ),
            )
    }

    @ParameterizedTest(name = "when given an address with {0}")
    @MethodSource("provideAddresses")
    fun `manualAddressDataToSingleLineAddress returns a corresponding address string`(
        expectedAddressString: String,
        addressLineOne: String,
        townOrCity: String,
        postcode: String,
        addressLineTwo: String?,
        county: String?,
    ) {
        assertEquals(
            expectedAddressString,
            AddressDataModel.manualAddressDataToSingleLineAddress(
                addressLineOne,
                townOrCity,
                postcode,
                addressLineTwo,
                county,
            ),
        )
    }

    @Test
    fun `fromManualAddressData returns a corresponding AddressDataModel`() {
        val expectedAddressDataModel =
            AddressDataModel(
                singleLineAddress = "Flat 10, 1 Example Road, Townville, Countyshire, EG1 2AB",
                townName = "Townville",
                postcode = "EG1 2AB",
                localAuthorityId = 1,
            )

        val addressDataModel =
            AddressDataModel.fromManualAddressData(
                "Flat 10",
                "Townville",
                "EG1 2AB",
                "1 Example Road",
                "Countyshire",
                1,
            )

        assertEquals(expectedAddressDataModel, addressDataModel)
    }
}
