package uk.gov.communities.prsdb.webapp.services

import org.apache.http.HttpException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.clients.OSPlacesClient
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import kotlin.test.assertEquals

class OSPlacesAddressLookupServiceTests {
    private lateinit var mockOSPlacesClient: OSPlacesClient
    private lateinit var addressLookupService: OSPlacesAddressLookupService

    @BeforeEach
    fun setup() {
        mockOSPlacesClient = mock()
        addressLookupService = OSPlacesAddressLookupService(mockOSPlacesClient)
    }

    @Test
    fun `searchByPostcode returns a corresponding list of addresses given a valid postcode`() {
        val addressesJSON =
            "{'results':[" +
                "{'DPA':{'ADDRESS':'1, Example Road, EG','LOCAL_CUSTODIAN_CODE':100,'UPRN':'1234','BUILDING_NUMBER':1,'POSTCODE':'EG'}}," +
                "{'DPA':{'ADDRESS':'2, Example Road, EG','LOCAL_CUSTODIAN_CODE':101,'UPRN':'','BUILDING_NUMBER':2,'POSTCODE':'EG'}}," +
                "{'DPA':{'ADDRESS':'Main, Example Road, EG','LOCAL_CUSTODIAN_CODE':102,'UPRN':'','BUILDING_NAME':'Main','POSTCODE':'EG'}}" +
                "]}"
        val expectedAddresses =
            listOf(
                AddressDataModel("1, Example Road, EG", "100", 1234, buildingNumber = "1", postcode = "EG"),
                AddressDataModel("2, Example Road, EG", "101", buildingNumber = "2", postcode = "EG"),
                AddressDataModel("Main, Example Road, EG", "102", buildingName = "Main", postcode = "EG"),
            )

        whenever(
            mockOSPlacesClient.search(anyString(), anyString()),
        ).thenReturn(addressesJSON)

        val addresses = addressLookupService.search("", "")

        assertEquals(expectedAddresses, addresses)
    }

    @Test
    fun `searchByPostcode throws a HTTP error if the API call fails`() {
        doAnswer { throw HttpException() }.whenever(mockOSPlacesClient).search(anyString(), anyString())

        assertThrows<HttpException> { addressLookupService.search("", "EG") }
    }
}
