package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.helpers.extensions.JourneyDataExtensions.Companion.getLookedUpAddresses
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import kotlin.test.assertEquals

class PropertyRegistrationJourneyDataHelperTests {
    private lateinit var mockLocalAuthorityService: LocalAuthorityService
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        mockLocalAuthorityService = mock()
        journeyDataBuilder = JourneyDataBuilder.propertyDefault(mockLocalAuthorityService)
    }

    @Test
    fun `getAddress returns the selected address`() {
        val selectedAddress = "1 Example Address, EG1 2AB"
        val localAuthority = createLocalAuthority()
        val mockJourneyData =
            journeyDataBuilder.withSelectedAddress(selectedAddress, localAuthority = localAuthority).build()
        val expectedAddressDataModel = AddressDataModel(selectedAddress, localAuthorityId = localAuthority.id)

        val addressDataModel = PropertyRegistrationJourneyDataHelper.getAddress(mockJourneyData, mockJourneyData.getLookedUpAddresses())

        assertEquals(expectedAddressDataModel, addressDataModel)
    }

    @Test
    fun `getAddress returns the manual address`() {
        val addressLineOne = "1 Example Address"
        val townOrCity = "Townville"
        val postcode = "EG1 2AB"
        val localAuthority = createLocalAuthority()
        val mockJourneyData =
            journeyDataBuilder
                .withManualAddress(addressLineOne, townOrCity, postcode, localAuthority)
                .build()
        val expectedAddressDataModel =
            AddressDataModel.fromManualAddressData(
                addressLineOne,
                townOrCity,
                postcode,
                localAuthorityId = localAuthority.id,
            )

        whenever(mockLocalAuthorityService.retrieveLocalAuthorityByCustodianCode(localAuthority.custodianCode)).thenReturn(
            localAuthority,
        )

        val addressDataModel = PropertyRegistrationJourneyDataHelper.getAddress(mockJourneyData, mockJourneyData.getLookedUpAddresses())
        assertEquals(expectedAddressDataModel, addressDataModel)
    }

    @Test
    fun `getPropertyType returns the corresponding property type`() {
        val expectedPropertyType = PropertyType.DETACHED_HOUSE
        val mockJourneyData = journeyDataBuilder.withPropertyType(expectedPropertyType).build()

        val propertyType = PropertyRegistrationJourneyDataHelper.getPropertyType(mockJourneyData)

        assertEquals(expectedPropertyType, propertyType)
    }

    @Test
    fun `getCustomPropertyType returns the customPropertyType string`() {
        val expectedPropertyType = "End terrace"
        val mockJourneyData = journeyDataBuilder.withPropertyType(PropertyType.OTHER, expectedPropertyType).build()

        val customPropertyType = PropertyRegistrationJourneyDataHelper.getCustomPropertyType(mockJourneyData)

        assertEquals(expectedPropertyType, customPropertyType)
    }

    @Test
    fun `getOwnershipType returns the corresponding ownership type`() {
        val expectedOwnershipType = OwnershipType.FREEHOLD
        val mockJourneyData = journeyDataBuilder.withOwnershipType(expectedOwnershipType).build()

        val ownershipType = PropertyRegistrationJourneyDataHelper.getOwnershipType(mockJourneyData)

        assertEquals(expectedOwnershipType, ownershipType)
    }

    @Test
    fun `getIsOccupied returns true if the property is occupied`() {
        val mockJourneyData = journeyDataBuilder.withTenants(households = 1, people = 1).build()

        val isOccupied = PropertyRegistrationJourneyDataHelper.getIsOccupied(mockJourneyData)!!

        assertTrue(isOccupied)
    }

    @Test
    fun `getIsOccupied returns false if the property is not occupied`() {
        val mockJourneyData = journeyDataBuilder.withNoTenants().build()

        val isOccupied = PropertyRegistrationJourneyDataHelper.getIsOccupied(mockJourneyData)!!

        assertFalse(isOccupied)
    }

    @Test
    fun `getNumberOfHouseholds returns the number of households`() {
        val expectedNumberOfHouseholds = 2
        val mockJourneyData = journeyDataBuilder.withTenants(expectedNumberOfHouseholds, people = 1).build()

        val numberOfHouseholds = PropertyRegistrationJourneyDataHelper.getNumberOfHouseholds(mockJourneyData)

        assertEquals(expectedNumberOfHouseholds, numberOfHouseholds)
    }

    @Test
    fun `getNumberOfHouseholds returns 0 when there are no households`() {
        val expectedNumberOfHouseholds = 0
        val mockJourneyData = journeyDataBuilder.withTenants(expectedNumberOfHouseholds, people = 1).build()

        val numberOfHouseholds = PropertyRegistrationJourneyDataHelper.getNumberOfHouseholds(mockJourneyData)

        assertEquals(expectedNumberOfHouseholds, numberOfHouseholds)
    }

    @Test
    fun `getNumberOfTenants returns the number of people in the house`() {
        val expectedNumberOfTenants = 2
        val mockJourneyData = journeyDataBuilder.withTenants(households = 1, expectedNumberOfTenants).build()

        val numberOfTenants = PropertyRegistrationJourneyDataHelper.getNumberOfTenants(mockJourneyData)

        assertEquals(expectedNumberOfTenants, numberOfTenants)
    }

    @Test
    fun `getNumberOfTenants returns 0 when there are no people in the house`() {
        val expectedNumberOfTenants = 0
        val mockJourneyData = journeyDataBuilder.withTenants(households = 1, expectedNumberOfTenants).build()

        val numberOfTenants = PropertyRegistrationJourneyDataHelper.getNumberOfTenants(mockJourneyData)

        assertEquals(expectedNumberOfTenants, numberOfTenants)
    }

    @Test
    fun `getLicensingType returns the licensing type`() {
        val expectedLicensingType = LicensingType.SELECTIVE_LICENCE
        val mockJourneyData = journeyDataBuilder.withLicensingType(expectedLicensingType).build()

        val licensingType = PropertyRegistrationJourneyDataHelper.getLicensingType(mockJourneyData)

        assertEquals(expectedLicensingType, licensingType)
    }

    @Test
    fun `getLicenceNumber returns the selective license number`() {
        val expectedLicenseNumber = "L1234"
        val mockJourneyData =
            journeyDataBuilder.withLicensingType(LicensingType.SELECTIVE_LICENCE, expectedLicenseNumber).build()

        val licenseNumber = PropertyRegistrationJourneyDataHelper.getLicenseNumber(mockJourneyData)

        assertEquals(expectedLicenseNumber, licenseNumber)
    }

    @Test
    fun `getLicenceNumber returns the HmoMandatoryLicence number `() {
        val expectedLicenseNumber = "L1234"
        val mockJourneyData =
            journeyDataBuilder.withLicensingType(LicensingType.HMO_MANDATORY_LICENCE, expectedLicenseNumber).build()

        val licenseNumber = PropertyRegistrationJourneyDataHelper.getLicenseNumber(mockJourneyData)

        assertEquals(expectedLicenseNumber, licenseNumber)
    }

    @Test
    fun `getLicenceNumber returns the HmoAdditionalLicence number `() {
        val expectedLicenseNumber = "L1234"
        val mockJourneyData =
            journeyDataBuilder.withLicensingType(LicensingType.HMO_ADDITIONAL_LICENCE, expectedLicenseNumber).build()

        val licenseNumber = PropertyRegistrationJourneyDataHelper.getLicenseNumber(mockJourneyData)

        assertEquals(expectedLicenseNumber, licenseNumber)
    }

    @Test
    fun `getLicenceNumber returns the no licence number `() {
        val expectedLicenseNumber = ""
        val mockJourneyData = journeyDataBuilder.withLicensingType(LicensingType.NO_LICENSING).build()

        val licenseNumber = PropertyRegistrationJourneyDataHelper.getLicenseNumber(mockJourneyData)

        assertEquals(expectedLicenseNumber, licenseNumber)
    }
}
