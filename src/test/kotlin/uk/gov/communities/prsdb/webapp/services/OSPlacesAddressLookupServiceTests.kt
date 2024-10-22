package uk.gov.communities.prsdb.webapp.services

import org.apache.http.HttpException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
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
                "{'DPA':{'ADDRESS':'1, Example Road, EG','POSTCODE':'EG','BUILDING_NUMBER':1}}," +
                "{'DPA':{'ADDRESS':'Main Building, Example Road, EG','POSTCODE':'EG','BUILDING_NAME':'Main Building'}}," +
                "{'DPA':{'ADDRESS':'PO1, Example Road, EG','POSTCODE':'EG','PO_BOX_NUMBER':'PO1'}}," +
                "]}"
        val expectedAddresses =
            listOf(
                AddressDataModel("1, Example Road, EG", "EG", 1),
                AddressDataModel("Main Building, Example Road, EG", "EG", buildingName = "Main Building"),
                AddressDataModel("PO1, Example Road, EG", "EG", poBoxNumber = "PO1"),
            )

        `when`(
            mockOSPlacesClient.searchByPostcode(anyString()),
        ).thenReturn(addressesJSON)

        assertEquals(expectedAddresses, addressLookupService.searchByPostcode("EG"))
    }

    @Test
    fun `searchByPostcode throws a HTTP error if the API call fails`() {
        doAnswer { throw HttpException() }.`when`(mockOSPlacesClient).searchByPostcode(anyString())

        assertThrows<HttpException> { addressLookupService.searchByPostcode("EG") }
    }
}
