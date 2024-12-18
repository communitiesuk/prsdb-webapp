package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class AddressDataServiceTests {
    @Mock
    private lateinit var mockHttpSession: HttpSession

    @Mock
    private lateinit var mockJourneyDataService: JourneyDataService

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

    @Test
    fun `getCachedAddressRegisteredResult returns null if no results are cached`() {
        val uprn = 1234.toLong()
        whenever(mockHttpSession.getAttribute("addressRegisteredResults")).thenReturn(null)
        assertNull(addressDataService.getCachedAddressRegisteredResult(uprn))
    }

    @Test
    fun `getCachedAddressRegisteredResult returns null if no matching result is cached`() {
        val uprn = 1234.toLong()
        whenever(mockHttpSession.getAttribute("addressRegisteredResults")).thenReturn(mutableMapOf(5678.toString() to true))
        assertNull(addressDataService.getCachedAddressRegisteredResult(uprn))
    }

    @Test
    fun `getCachedAddressRegisteredResult returns true if the cached result is true`() {
        val uprn = 1234.toLong()
        whenever(mockHttpSession.getAttribute("addressRegisteredResults")).thenReturn(mutableMapOf(uprn.toString() to true))
        assertTrue(addressDataService.getCachedAddressRegisteredResult(uprn) ?: false)
    }

    @Test
    fun `getCachedAddressRegisteredResult returns false if the cached result is false`() {
        val uprn = 1234.toLong()
        whenever(mockHttpSession.getAttribute("addressRegisteredResults")).thenReturn(mutableMapOf(uprn.toString() to false))
        assertFalse(addressDataService.getCachedAddressRegisteredResult(uprn) ?: true)
    }

    @Test
    fun `setCachedAddressRegisteredResult adds the result keyed by the uprn to the existing results cache`() {
        whenever(mockHttpSession.getAttribute("addressRegisteredResults")).thenReturn(mutableMapOf(5678.toString() to true))

        val uprn = 1234.toLong()

        val expectedNewCache = mutableMapOf(5678.toString() to true, uprn.toString() to false)

        addressDataService.setCachedAddressRegisteredResult(uprn, false)
        val addressRegisteredResultCaptor = captor<MutableMap<String, Boolean>>()
        verify(mockHttpSession).setAttribute(eq("addressRegisteredResults"), addressRegisteredResultCaptor.capture())
        Assertions.assertEquals(expectedNewCache, addressRegisteredResultCaptor.value)
    }

    @Test
    fun `getManualAddress returns an address from journey data`() {
        val addressLineOne = "1 Example Address"
        val townOrCity = "Townville"
        val postcode = "EG1 2AB"
        val expectedAddressDataModel = AddressDataModel.fromManualAddressData(addressLineOne, townOrCity, postcode)
        val manualAddressPathSegment = "manual-address"
        val mockJourneyData: JourneyData = mutableMapOf()

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                manualAddressPathSegment,
                "addressLineOne",
            ),
        ).thenReturn(addressLineOne)

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                manualAddressPathSegment,
                "townOrCity",
            ),
        ).thenReturn(townOrCity)

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                manualAddressPathSegment,
                "postcode",
            ),
        ).thenReturn(postcode)

        val addressDataModel = addressDataService.getManualAddress(mockJourneyDataService, mockJourneyData, manualAddressPathSegment)

        assertEquals(expectedAddressDataModel, addressDataModel)
    }
}
