package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.communities.prsdb.webapp.constants.ENGLAND
import uk.gov.communities.prsdb.webapp.constants.UNASSIGNED
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
                localCouncilId = 1,
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

    @Test
    fun `fromManualAddressData uses the given country`() {
        // Act
        val addressDataModel =
            AddressDataModel.fromManualAddressData("1 Example Road", "Townville", "EG1 2AB", country = "France")

        // Assert
        assertEquals("France", addressDataModel.country)
    }

    @Test
    fun `fromManualAddressData defaults country to England when no country is given`() {
        // Act
        val addressDataModel =
            AddressDataModel.fromManualAddressData("1 Example Road", "Townville", "EG1 2AB", country = null)

        // Assert
        assertEquals(ENGLAND, addressDataModel.country)
    }

    @Test
    fun `toMultiLineAddress appends the country when address components are available`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "1 Castle Street, Cardiff, CF10 1AA",
                buildingNumber = "1",
                streetName = "Castle Street",
                townName = "Cardiff",
                postcode = "CF10 1AA",
                country = "Wales",
            )

        assertEquals("1 Castle Street\nCardiff\nCF10 1AA\nWales", addressDataModel.toMultiLineAddress())
    }

    @Test
    fun `toMultiLineAddress appends the country after an appended postcode`() {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "1 Example Road, Townville",
                postcode = "EG1 2AB",
            )

        assertEquals("1 Example Road\nTownville\nEG1 2AB\nEngland", addressDataModel.toMultiLineAddress())
    }

    @ParameterizedTest
    @ValueSource(strings = [UNASSIGNED, "", " "])
    fun `toMultiLineAddress omits the country when it is unassigned or blank`(country: String) {
        val addressDataModel =
            AddressDataModel(
                singleLineAddress = "1 Example Road, Townville, EG1 2AB",
                postcode = "EG1 2AB",
                country = country,
            )

        assertEquals("1 Example Road\nTownville\nEG1 2AB", addressDataModel.toMultiLineAddress())
    }
}
