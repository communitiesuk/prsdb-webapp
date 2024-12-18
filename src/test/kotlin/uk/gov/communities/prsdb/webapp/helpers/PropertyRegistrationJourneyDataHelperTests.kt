package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.test.assertEquals

class PropertyRegistrationJourneyDataHelperTests {
    private lateinit var mockJourneyDataService: JourneyDataService
    private lateinit var mockAddressDataService: AddressDataService

    private val mockJourneyData: JourneyData = mutableMapOf()

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        mockAddressDataService = mock()
    }

    @Test
    fun `getAddress returns the selected address`() {
        val selectedAddress = "1 Example Address, EG1 2AB"

        val expectedAddressDataModel = AddressDataModel(selectedAddress)

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.SelectAddress.urlPathSegment,
                "address",
            ),
        ).thenReturn(selectedAddress)

        whenever(
            mockAddressDataService.getAddressData(selectedAddress),
        ).thenReturn(expectedAddressDataModel)

        val addressDataModel =
            PropertyRegistrationJourneyDataHelper.getAddress(
                mockJourneyDataService,
                mockJourneyData,
                mockAddressDataService,
            )

        assertEquals(expectedAddressDataModel, addressDataModel)
    }

    @Test
    fun `getAddress returns the manual address`() {
        val addressLineOne = "1 Example Address"
        val townOrCity = "Townville"
        val postcode = "EG1 2AB"
        val expectedAddressDataModel = AddressDataModel.fromManualAddressData(addressLineOne, townOrCity, postcode)

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.SelectAddress.urlPathSegment,
                "address",
            ),
        ).thenReturn(MANUAL_ADDRESS_CHOSEN)

        whenever(
            mockAddressDataService
                .getManualAddress(mockJourneyDataService, mockJourneyData, RegisterPropertyStepId.ManualAddress.urlPathSegment),
        ).thenReturn(expectedAddressDataModel)

        val addressDataModel =
            PropertyRegistrationJourneyDataHelper.getAddress(
                mockJourneyDataService,
                mockJourneyData,
                mockAddressDataService,
            )
        assertEquals(expectedAddressDataModel, addressDataModel)
    }

    @Test
    fun `getPropertyType returns the corresponding property type`() {
        val expectedPropertyType = PropertyType.DETACHED_HOUSE

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.PropertyType.urlPathSegment,
                "propertyType",
            ),
        ).thenReturn(expectedPropertyType.name)

        val propertyType = PropertyRegistrationJourneyDataHelper.getPropertyType(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedPropertyType, propertyType)
    }

    @Test
    fun `getCustomPropertyType returns the customPropertyType string`() {
        val expectedPropertyType = "End terrace"

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.PropertyType.urlPathSegment,
                "customPropertyType",
            ),
        ).thenReturn(expectedPropertyType)

        val customPropertyType = PropertyRegistrationJourneyDataHelper.getCustomPropertyType(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedPropertyType, customPropertyType)
    }

    @Test
    fun `getOwnershipType returns the corresponding ownership type`() {
        val expectedOwnershipType = OwnershipType.FREEHOLD

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.OwnershipType.urlPathSegment,
                "ownershipType",
            ),
        ).thenReturn(expectedOwnershipType.name)

        val ownershipType = PropertyRegistrationJourneyDataHelper.getOwnershipType(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedOwnershipType, ownershipType)
    }

    @Test
    fun `getLandlordType returns the corresponding landlord type`() {
        val expectedLandlordType = LandlordType.SOLE

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.LandlordType.urlPathSegment,
                "landlordType",
            ),
        ).thenReturn(expectedLandlordType.name)

        val landlordType = PropertyRegistrationJourneyDataHelper.getLandlordType(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedLandlordType, landlordType)
    }
}
