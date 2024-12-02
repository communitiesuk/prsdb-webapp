package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class AddressDataServiceTests {
    @Mock
    private lateinit var mockHttpSession: HttpSession

    private lateinit var addressDataService: AddressDataService

    @BeforeEach
    fun setup() {
        addressDataService = AddressDataService(mockHttpSession)
    }

    @Test
    fun `getAddressData returns the AddressDataModel that corresponds with the given address`() {
        val addressDataJSON =
            Json.encodeToString(
                listOf(
                    AddressDataModel("1, Example Road, EG", "100", 1234, buildingNumber = "1", postcode = "EG"),
                    AddressDataModel("2, Example Road, EG", "101", buildingNumber = "2", postcode = "EG"),
                    AddressDataModel("Main, Example Road, EG", "102", buildingName = "Main", postcode = "EG"),
                ).associateBy { it.singleLineAddress },
            )
        val expectedAddressData =
            AddressDataModel("1, Example Road, EG", "100", 1234, buildingNumber = "1", postcode = "EG")

        whenever(mockHttpSession.getAttribute("addressData")).thenReturn(addressDataJSON)

        val addressData = addressDataService.getAddressData("1, Example Road, EG")

        assertEquals(expectedAddressData, addressData)
    }

    @Test
    fun `getAddressData returns null when given an invalid address`() {
        val addressDataJSON =
            Json.encodeToString(
                listOf(
                    AddressDataModel("1, Example Road, EG", "100", 1234, buildingNumber = "1", postcode = "EG"),
                    AddressDataModel("2, Example Road, EG", "101", buildingNumber = "2", postcode = "EG"),
                    AddressDataModel("Main, Example Road, EG", "102", buildingName = "Main", postcode = "EG"),
                ).associateBy { it.singleLineAddress },
            )

        whenever(mockHttpSession.getAttribute("addressData")).thenReturn(addressDataJSON)

        val addressData = addressDataService.getAddressData("invalid address")

        assertNull(addressData)
    }

    @Test
    fun `setAddressData stores the given address data as a serialized map`() {
        val addressDataList =
            listOf(
                AddressDataModel("1, Example Road, EG", "100", 1234, buildingNumber = "1", postcode = "EG"),
                AddressDataModel("2, Example Road, EG", "101", buildingNumber = "2", postcode = "EG"),
                AddressDataModel("Main, Example Road, EG", "102", buildingName = "Main", postcode = "EG"),
            )
        val expectedAddressDataString = Json.encodeToString(addressDataList.associateBy { it.singleLineAddress })

        addressDataService.setAddressData(addressDataList)

        val addressDataStringCaptor = captor<String>()
        verify(mockHttpSession).setAttribute(eq("addressData"), addressDataStringCaptor.capture())
        Assertions.assertEquals(expectedAddressDataString, addressDataStringCaptor.value)
    }
}
