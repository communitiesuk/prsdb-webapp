package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
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
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.ManualAddress.urlPathSegment,
                "addressLineOne",
            ),
        ).thenReturn(addressLineOne)

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.ManualAddress.urlPathSegment,
                "townOrCity",
            ),
        ).thenReturn(townOrCity)

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.ManualAddress.urlPathSegment,
                "postcode",
            ),
        ).thenReturn(postcode)

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

        val propertyType =
            PropertyRegistrationJourneyDataHelper.getPropertyType(mockJourneyDataService, mockJourneyData)

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

        val customPropertyType =
            PropertyRegistrationJourneyDataHelper.getCustomPropertyType(mockJourneyDataService, mockJourneyData)

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

        val ownershipType =
            PropertyRegistrationJourneyDataHelper.getOwnershipType(mockJourneyDataService, mockJourneyData)

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

        val landlordType =
            PropertyRegistrationJourneyDataHelper.getLandlordType(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedLandlordType, landlordType)
    }

    @Test
    fun `getIsOccupied returns true if the property is occupied`() {
        whenever(
            mockJourneyDataService.getFieldBooleanValue(
                mockJourneyData,
                RegisterPropertyStepId.Occupancy.urlPathSegment,
                "occupied",
            ),
        ).thenReturn(true)

        assertTrue(
            PropertyRegistrationJourneyDataHelper.getIsOccupied(mockJourneyDataService, mockJourneyData) ?: false,
        )
    }

    @Test
    fun `getIsOccupied returns false if the property is not occupied`() {
        whenever(
            mockJourneyDataService.getFieldBooleanValue(
                mockJourneyData,
                RegisterPropertyStepId.Occupancy.urlPathSegment,
                "occupied",
            ),
        ).thenReturn(false)

        assertFalse(
            PropertyRegistrationJourneyDataHelper.getIsOccupied(mockJourneyDataService, mockJourneyData) ?: true,
        )
    }

    @Test
    fun `getNumberOfHouseholds returns the number of households`() {
        val expectedNumberOfHouseholds = 2
        whenever(
            mockJourneyDataService.getFieldIntegerValue(
                mockJourneyData,
                RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
                "numberOfHouseholds",
            ),
        ).thenReturn(expectedNumberOfHouseholds)

        val numberOfHouseholds =
            PropertyRegistrationJourneyDataHelper.getNumberOfHouseholds(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedNumberOfHouseholds, numberOfHouseholds)
    }

    @Test
    fun `getNumberOfHouseholds returns 0 when there are no households`() {
        val expectedNumberOfHouseholds = 0
        whenever(
            mockJourneyDataService.getFieldIntegerValue(
                mockJourneyData,
                RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
                "numberOfHouseholds",
            ),
        ).thenReturn(null)

        val numberOfHouseholds =
            PropertyRegistrationJourneyDataHelper.getNumberOfHouseholds(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedNumberOfHouseholds, numberOfHouseholds)
    }

    @Test
    fun `getNumberOfTenants returns the number of people in the house`() {
        val expectedNumberOfTenants = 2
        whenever(
            mockJourneyDataService.getFieldIntegerValue(
                mockJourneyData,
                RegisterPropertyStepId.NumberOfPeople.urlPathSegment,
                "numberOfPeople",
            ),
        ).thenReturn(expectedNumberOfTenants)

        val numberOfTenants =
            PropertyRegistrationJourneyDataHelper.getNumberOfTenants(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedNumberOfTenants, numberOfTenants)
    }

    @Test
    fun `getNumberOfTenants returns 0 when there are no people in the house`() {
        val expectedNumberOfTenants = 0
        whenever(
            mockJourneyDataService.getFieldIntegerValue(
                mockJourneyData,
                RegisterPropertyStepId.NumberOfPeople.urlPathSegment,
                "numberOfPeople",
            ),
        ).thenReturn(null)

        val numberOfTenants =
            PropertyRegistrationJourneyDataHelper.getNumberOfTenants(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedNumberOfTenants, numberOfTenants)
    }

    @Test
    fun `getLicensingType returns the licensing type`() {
        val expectedLicensingType = LicensingType.SELECTIVE_LICENCE
        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.LicensingType.urlPathSegment,
                "licensingType",
            ),
        ).thenReturn(LicensingType.SELECTIVE_LICENCE.name)

        val licensingType =
            PropertyRegistrationJourneyDataHelper.getLicensingType(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedLicensingType, licensingType)
    }

    @Test
    fun `getLicenceNumber returns the selective license number`() {
        val expectedLicenseNumber = "L1234"

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.LicensingType.urlPathSegment,
                "licensingType",
            ),
        ).thenReturn(LicensingType.SELECTIVE_LICENCE.name)

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.SelectiveLicence.urlPathSegment,
                "licenceNumber",
            ),
        ).thenReturn(expectedLicenseNumber)

        val licenseNumber =
            PropertyRegistrationJourneyDataHelper.getLicenseNumber(mockJourneyDataService, mockJourneyData)

        assertEquals(expectedLicenseNumber, licenseNumber)
    }

    @Test
    fun `getLicenceNumber returns the HmoMandatoryLicence number `() {
        val expectedLicenseNumber = "L1234"

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.LicensingType.urlPathSegment,
                "licensingType",
            ),
        ).thenReturn(LicensingType.HMO_MANDATORY_LICENCE.name)

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.HmoMandatoryLicence.urlPathSegment,
                "licenceNumber",
            ),
        ).thenReturn(expectedLicenseNumber)

        val licenseNumber =
            PropertyRegistrationJourneyDataHelper.getLicenseNumber(
                mockJourneyDataService,
                mockJourneyData,
            )

        assertEquals(expectedLicenseNumber, licenseNumber)
    }

    @Test
    fun `getLicenceNumber returns the HmoAdditionalLicence number `() {
        val expectedLicenseNumber = "L1234"

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.LicensingType.urlPathSegment,
                "licensingType",
            ),
        ).thenReturn(LicensingType.HMO_ADDITIONAL_LICENCE.name)

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.HmoAdditionalLicence.urlPathSegment,
                "licenceNumber",
            ),
        ).thenReturn(expectedLicenseNumber)

        val licenseNumber =
            PropertyRegistrationJourneyDataHelper.getLicenseNumber(
                mockJourneyDataService,
                mockJourneyData,
            )

        assertEquals(expectedLicenseNumber, licenseNumber)
    }

    @Test
    fun `getLicenceNumber returns the no licence number `() {
        val expectedLicenseNumber = ""

        whenever(
            mockJourneyDataService.getFieldStringValue(
                mockJourneyData,
                RegisterPropertyStepId.LicensingType.urlPathSegment,
                "licensingType",
            ),
        ).thenReturn(LicensingType.NO_LICENSING.name)

        val licenseNumber =
            PropertyRegistrationJourneyDataHelper.getLicenseNumber(
                mockJourneyDataService,
                mockJourneyData,
            )

        assertEquals(expectedLicenseNumber, licenseNumber)
    }
}
