package uk.gov.communities.prsdb.webapp.services

import org.apache.http.HttpException
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.clients.OSPlacesClient
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class OSPlacesAddressLookupServiceTests {
    @Mock
    private lateinit var mockOSPlacesClient: OSPlacesClient

    @Mock
    private lateinit var mockLocalAuthorityService: LocalAuthorityService

    @InjectMocks
    private lateinit var addressLookupService: OSPlacesAddressLookupService

    @ParameterizedTest(name = "when {0}")
    @MethodSource("provideAddresses")
    fun `search returns a corresponding list of addresses`(
        restrictToEngland: Boolean,
        expectedAddresses: List<AddressDataModel>,
    ) {
        val invalidCustodianCode = 10000000

        val addressesJSON =
            """
            {
                "results": [
                    {
                        "DPA": {
                            "ADDRESS": "1, Example Road, EG",
                            "LOCAL_CUSTODIAN_CODE": 1,
                            "UPRN": "1234",
                            "BUILDING_NUMBER": 1,
                            "POSTCODE": "EG",
                            "COUNTRY_CODE": "E"
                        }
                    },
                    {
                        "DPA": {
                            "ADDRESS": "2, Example Road, EG",
                            "LOCAL_CUSTODIAN_CODE": 2,
                            "UPRN": "",
                            "BUILDING_NUMBER": 2,
                            "POSTCODE": "EG",
                            "COUNTRY_CODE": "E"
                        }
                    },
                    {
                        "DPA": {
                            "ADDRESS": "Main, Example Road, EG",
                            "LOCAL_CUSTODIAN_CODE": $invalidCustodianCode,
                            "UPRN": "",
                            "BUILDING_NAME": "Main",
                            "POSTCODE": "EG",
                            "COUNTRY_CODE": "E"
                        }
                    },
                    {
                        "DPA": {
                            "ADDRESS": "Welsh House, Non-England Street, WG",
                            "LOCAL_CUSTODIAN_CODE": 100,
                            "UPRN": "5678",
                            "BUILDING_NAME": Welsh House,
                            "POSTCODE": "WG",
                            "COUNTRY_CODE": "W"
                        }
                    },
                ]
            }
            """

        whenever(mockOSPlacesClient.search(anyString(), anyString())).thenReturn(addressesJSON)

        whenever(mockLocalAuthorityService.retrieveLocalAuthorityByCustodianCode(anyString())).then {
            val custodianCode =
                it.arguments
                    .first()
                    .toString()
                    .toInt()
            if (custodianCode == invalidCustodianCode) null else LocalAuthority(custodianCode)
        }

        val addresses = addressLookupService.search("", "", restrictToEngland)

        assertEquals(expectedAddresses, addresses)
    }

    @Test
    fun `search throws a HTTP error if the API call fails`() {
        doAnswer { throw HttpException() }.whenever(mockOSPlacesClient).search(anyString(), anyString())

        assertThrows<HttpException> { addressLookupService.search("", "EG") }
    }

    companion object {
        @JvmStatic
        fun provideAddresses(): List<Arguments> {
            val englishAddresses =
                listOf(
                    AddressDataModel(
                        singleLineAddress = "1, Example Road, EG",
                        localAuthorityId = 1,
                        uprn = 1234,
                        buildingNumber = "1",
                        postcode = "EG",
                    ),
                    AddressDataModel(
                        singleLineAddress = "2, Example Road, EG",
                        localAuthorityId = 2,
                        buildingNumber = "2",
                        postcode = "EG",
                    ),
                    AddressDataModel(
                        singleLineAddress = "Main, Example Road, EG",
                        buildingName = "Main",
                        postcode = "EG",
                    ),
                )

            val welshAddress =
                AddressDataModel(
                    singleLineAddress = "Welsh House, Non-England Street, WG",
                    uprn = 5678,
                    buildingName = "Welsh House",
                    postcode = "WG",
                )

            return listOf(
                arguments(named("restricting to England", true), englishAddresses),
                arguments(named("not restricting to England", false), englishAddresses + welshAddress),
            )
        }
    }
}
