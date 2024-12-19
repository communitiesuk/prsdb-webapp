package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.test.assertEquals

class JourneyDataHelperTests {
    private lateinit var mockJourneyDataService: JourneyDataService

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
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

        val addressDataModel = JourneyDataHelper.getManualAddress(mockJourneyDataService, mockJourneyData, manualAddressPathSegment)

        assertEquals(expectedAddressDataModel, addressDataModel)
    }
}
