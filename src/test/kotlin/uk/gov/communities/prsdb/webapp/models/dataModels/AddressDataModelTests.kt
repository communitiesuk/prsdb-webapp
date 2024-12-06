package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class AddressDataModelTests {
    companion object {
        @JvmStatic
        fun provideManualAddressFieldsAndAddressDataModels() =
            listOf(
                Arguments.of(
                    "Flat 10",
                    "Townville",
                    "EG1 2AB",
                    "1 Example Road",
                    "Countyshire",
                    AddressDataModel(
                        singleLineAddress = "Flat 10, 1 Example Road, Townville, EG1 2AB, Countyshire",
                        townName = "Townville",
                        postcode = "EG1 2AB",
                    ),
                ),
                Arguments.of(
                    "1 Example Road",
                    "Townville",
                    "EG1 2AB",
                    null,
                    "Countyshire",
                    AddressDataModel(
                        singleLineAddress = "1 Example Road, Townville, EG1 2AB, Countyshire",
                        townName = "Townville",
                        postcode = "EG1 2AB",
                    ),
                ),
                Arguments.of(
                    "Flat 10",
                    "Townville",
                    "EG1 2AB",
                    "1 Example Road",
                    null,
                    AddressDataModel(
                        singleLineAddress = "Flat 10, 1 Example Road, Townville, EG1 2AB",
                        townName = "Townville",
                        postcode = "EG1 2AB",
                    ),
                ),
                Arguments.of(
                    "1 Example Road",
                    "Townville",
                    "EG1 2AB",
                    null,
                    null,
                    AddressDataModel(
                        singleLineAddress = "1 Example Road, Townville, EG1 2AB",
                        townName = "Townville",
                        postcode = "EG1 2AB",
                    ),
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("provideManualAddressFieldsAndAddressDataModels")
    fun `parseAddressDataModel returns a corresponding AddressDataModel`(
        addressLineOne: String,
        townOrCity: String,
        postcode: String,
        addressLineTwo: String?,
        county: String?,
        expectedAddressDataModel: AddressDataModel,
    ) {
        assertEquals(
            expectedAddressDataModel,
            AddressDataModel.parseAddressDataModel(addressLineOne, townOrCity, postcode, addressLineTwo, county),
        )
    }
}
