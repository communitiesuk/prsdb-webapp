package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.mockObjects.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import kotlin.test.assertEquals

class JourneyDataHelperTests {
    private lateinit var mockAddressDataService: AddressDataService
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        mockAddressDataService = mock()
        journeyDataBuilder = JourneyDataBuilder.landlordDefault(mockAddressDataService)
    }

    @Test
    fun `getManualAddress returns an address from journey data`() {
        val addressLineOne = "1 Example Address"
        val townOrCity = "Townville"
        val postcode = "EG1 2AB"
        val mockJourneyData = journeyDataBuilder.withManualAddress(addressLineOne, townOrCity, postcode).build()
        val expectedAddressDataModel = AddressDataModel.fromManualAddressData(addressLineOne, townOrCity, postcode)

        val addressDataModel = JourneyDataHelper.getManualAddress(mockJourneyData, "manual-address")

        assertEquals(expectedAddressDataModel, addressDataModel)
    }

    @Test
    fun `getLookupAddressHouseNameOrNumberAndPostcode returns a house name or number and postcode pair from journey data`() {
        val expectedHouseNameOrNumber = "1"
        val expectedPostcode = "EG1 2AB"
        val mockJourneyData =
            journeyDataBuilder
                .withLookupAddress(expectedHouseNameOrNumber, expectedPostcode)
                .build()

        val (houseNameOrNumber, postcode) =
            JourneyDataHelper.getLookupAddressHouseNameOrNumberAndPostcode(
                mockJourneyData,
                "lookup-address",
            )!!

        assertEquals(expectedHouseNameOrNumber, houseNameOrNumber)
        assertEquals(expectedPostcode, postcode)
    }
}
