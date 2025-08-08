package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JourneyDataHelperTests {
    private lateinit var mockLocalAuthorityService: LocalAuthorityService
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        mockLocalAuthorityService = mock()
        journeyDataBuilder = JourneyDataBuilder.landlordDefault(mockLocalAuthorityService)
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
    fun `getManualAddress returns an address with a local authority from journey data`() {
        val addressLineOne = "1 Example Address"
        val townOrCity = "Townville"
        val postcode = "EG1 2AB"
        val localAuthority = createLocalAuthority()
        val mockJourneyData =
            journeyDataBuilder.withManualAddress(addressLineOne, townOrCity, postcode, localAuthority).build()
        val expectedAddressDataModel =
            AddressDataModel.fromManualAddressData(
                addressLineOne,
                townOrCity,
                postcode,
                localAuthorityId = localAuthority.id,
            )

        val addressDataModel =
            JourneyDataHelper.getManualAddress(
                mockJourneyData,
                "manual-address",
                RegisterPropertyStepId.LocalAuthority.urlPathSegment,
            )

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

    @Nested
    inner class GetPageDataTests {
        @Test
        fun `returns page data from journeyData if subPageNumber is null`() {
            // Arrange
            val pageName = "testPage"
            val key = "testKey"
            val value = "testValue"
            val journeyData: JourneyData =
                mapOf(
                    pageName to mapOf(key to value),
                )

            // Act
            val pageData = JourneyDataHelper.getPageData(journeyData, pageName, null)

            // Assert
            assertEquals(pageData?.get(key), value)
        }

        @Test
        fun `returns null if page data is missing`() {
            // Arrange
            val pageName = "testPage"
            val journeyData: JourneyData = mapOf()

            // Act
            val pageData = JourneyDataHelper.getPageData(journeyData, pageName, null)

            // Assert
            assertNull(pageData)
        }

        @Test
        fun `returns subPage data from journeyData if subPageNumber is provided`() {
            // Arrange
            val pageName = "testPage"
            val subPageNumber = 12
            val key = "testKey"
            val value = "testValue"
            val journeyData: JourneyData =
                mapOf(
                    pageName to mapOf(subPageNumber.toString() to mapOf(key to value)),
                )

            // Act
            val subPageData = JourneyDataHelper.getPageData(journeyData, pageName, subPageNumber)

            // Assert
            assertEquals(subPageData?.get(key), value)
        }

        @Test
        fun `returns null if sub page data is missing`() {
            // Arrange
            val pageName = "testPage"
            val subPageNumber = 12
            val journeyData: JourneyData = mapOf(pageName to mutableMapOf<String, Any>())

            // Act
            val subPageData = JourneyDataHelper.getPageData(journeyData, pageName, subPageNumber)

            // Assert
            assertNull(subPageData)
        }
    }
}
